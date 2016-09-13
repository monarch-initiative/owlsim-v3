package org.monarchinitiative.owlsim.compute.enrich.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentEngine;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentQuery;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResult;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResultSet;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;
import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.kb.filter.TypeFilter;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class HypergeometricEnrichmentEngine implements EnrichmentEngine {

    private Logger LOG = Logger.getLogger(HypergeometricEnrichmentEngine.class);

    BMKnowledgeBase kb;

    // caches
    List<Integer> numHypothesesByFilter = new ArrayList<>();
    Integer numHypothesesGlobal;


    @Inject
    public HypergeometricEnrichmentEngine(BMKnowledgeBase kb) {
        super();
        this.kb = kb;
    }

    @Override
    public String getShortName() {
        return "hypergeometric";
    }

    @Override
    public void precompute() {
    }

    private int getNumHypotheses(String tid) {

        // check cache
        if (tid == null) {
            if (numHypothesesGlobal != null) {
                return numHypothesesGlobal;
            }
        }
        else {
            int cix = kb.getClassIndex(tid);
            if (numHypothesesByFilter.get(cix) != null) {
                return numHypothesesByFilter.get(cix);
            }

        }
        LOG.info("  1st time calc for |hyp| for "+tid);

        // for bonferoni correction, each class with >1 individual in the filter
        // counts as a distinct hypothesis
        EWAHCompressedBitmap filteredIndividualsBM = tid == null ? null : kb.getIndividualsBM(tid);
        int n=0;
        for (String cid : kb.getClassIdsInSignature()) {
            int cix = kb.getClassIndex(cid);
            if (filteredIndividualsBM != null) {
                if (kb.getIndividualsBM(cix).andCardinality(filteredIndividualsBM) < 2)
                    continue;

            }
            else if (kb.getIndividualsBM(cix).cardinality() < 2)
                continue;
            n++;
        }

        // populate cache
        if (tid == null) {
            numHypothesesGlobal = n;
        }
        else {
            int cix = kb.getClassIndex(tid);
            numHypothesesByFilter.set(cix, n);
        }

        return n;
    }

    @Override
    public EnrichmentResult calculateEnrichmentAgainstClass(
            EnrichmentQuery query, String enrichedClassId) {
        int popSize = kb.getIndividualIdsInSignature().size();
        EWAHCompressedBitmap sampleBM = getIndividualsBM(query);
        Filter filter = query.getFilter();
        EWAHCompressedBitmap filteredIndividualsBM = null;
        int numHypotheses = 0;
        if (filter != null && filter instanceof TypeFilter) {
            String tid = ((TypeFilter)filter).getTypeId();
            filteredIndividualsBM = kb.getIndividualsBM(tid);
            popSize = filteredIndividualsBM.cardinality();
            numHypotheses = getNumHypotheses(tid);
        }
        else {
            numHypotheses = getNumHypotheses(null);
        }
        if (popSize < 2) {
            return null;
        }
        return calculateEnrichmentAgainstClass(query, sampleBM, enrichedClassId, 
                popSize, numHypotheses, 
                filteredIndividualsBM);
    }

    public EnrichmentResult calculateEnrichmentAgainstClass(
            EnrichmentQuery query, 
            EWAHCompressedBitmap sampleBM,
            String enrichedClassId,
            int popSize,
            int numHypotheses,
            EWAHCompressedBitmap filteredIndividualsBM) {
        long t = System.currentTimeMillis();

        EWAHCompressedBitmap enrichedBM;

        // apply typeFilter if set.
        // example: typeFilter = NCBITaxon:9606
        if (filteredIndividualsBM == null)
            enrichedBM = kb.getIndividualsBM(enrichedClassId);
        else
            enrichedBM = kb.getIndividualsBM(enrichedClassId).and(filteredIndividualsBM);

        if (enrichedBM.cardinality() < 2) {
            return null;
        }
        int numShared = sampleBM.andCardinality(enrichedBM);
        if (numShared < 2) {
            return null;
        }
        int enrichedCardinality = enrichedBM.cardinality();
        int sampleCardinality = sampleBM.cardinality();
        HypergeometricDistribution hg = new HypergeometricDistribution(
                popSize, enrichedCardinality, sampleCardinality);
        int min = Math.min(enrichedCardinality, sampleCardinality);
        //double p = hg.cumulativeProbability(numShared, min);
        double p = hg.upperCumulativeProbability(numShared);

        //if (p == 0.0) {
        //LOG.error(numShared + " "+ enrichedCardinality +" "+sampleCardinality+" POP:"+popSize+" p="+p);
        //}

        long t2 = System.currentTimeMillis();
        double pCorrected = p * numHypotheses;

        return EnrichmentResultImpl.create(p, 
                pCorrected,
                enrichedClassId,
                numShared,
                enrichedCardinality,
                sampleCardinality
                );
    }

    @Override
    public EnrichmentResultSet calculateEnrichmentAgainstKb(
            EnrichmentQuery query) {

        List<EnrichmentResult> results = new ArrayList<>();
        for (String cid : kb.getClassIdsInSignature()) {
            EnrichmentResult r = calculateEnrichmentAgainstClass(query, cid);
            if (r != null)
                results.add(r);
        }
        EnrichmentResultSet rs = new EnrichmentResultSetImpl(results);
        rs.sortResults();
        EWAHCompressedBitmap blanket = kb.getSuperClassesBM(kb.getRootIndex());
        int blanketSize = 0;
        //int lastScore = 0;
        List<EnrichmentResult> nrresults = new ArrayList<>();
        for (EnrichmentResult r : rs.getResults()) {
            blanket = kb.getSuperClassesBM(r.getEnrichedClassId()).or(blanket);
            //int s = (int) (r.getScore() * 100);
            //if (lastScore == s || blanket.cardinality() > blanketSize) {
            if (blanket.cardinality() > blanketSize) {
                // filter out results if they are subsumed within previous
                // results (which must be either identically scored, or higher scored,
                // as results have been sorted)
                blanketSize = blanket.cardinality();
                nrresults.add(r);
            }
            //lastScore = s;
        }
        rs = new EnrichmentResultSetImpl(nrresults);
        rs.rankResults();
        return rs;

    }

    public EnrichmentResultSet calculateAllByAll(
            EnrichmentQuery query) {

        List<EnrichmentResult> results = new ArrayList<>();
        for (String cid: kb.getClassIdsInSignature()) {
            EnrichmentQuery cq = EnrichmentQueryImpl.create(cid);
            EnrichmentResultSet rs = calculateEnrichmentAgainstKb(cq);
            results.addAll(rs.getResults());
        }
        EnrichmentResultSetImpl rs = new EnrichmentResultSetImpl(results);
        rs.sortResults();
        rs.rankResults();
        return rs;
    }

    /**
     * Translates a query to a set of individuals expressed as a bitmap
     * 
     * The query can be either a class or an individual set. If the former, then we take the
     * extension of the class. If the latter, then the results are simply the bitmap form.
     * 
     * @param q
     * @return
     */
    protected EWAHCompressedBitmap getIndividualsBM(EnrichmentQuery q) {
        LOG.info("QUERY: "+q);
        if (q.getClassId() != null)
            return kb.getIndividualsBM(q.getClassId());
        else if (q.getIndividualIds() != null) {
            Set<Integer> ixs = q.getIndividualIds().stream().map( x -> kb.getIndividualIndex(x)).collect(Collectors.toSet());
            return EWAHUtils.converIndexSetToBitmap(ixs);
        }
        else {
            return null;
        }
    }

    public static EnrichmentEngine create(BMKnowledgeBase kb) {
        return new HypergeometricEnrichmentEngine(kb);
    }



}
