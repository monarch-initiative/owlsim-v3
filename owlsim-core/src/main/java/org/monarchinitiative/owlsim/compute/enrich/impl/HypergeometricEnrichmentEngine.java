package org.monarchinitiative.owlsim.compute.enrich.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentConfig;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentEngine;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentQuery;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResult;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResultSet;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentConfig.AnalysisType;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;
import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.kb.filter.TypeFilter;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Implementation of EnrichmentEngine that uses Fisher Exact (hypergeometric) tests
 * 
 * @author cjm
 *
 */
public class HypergeometricEnrichmentEngine implements EnrichmentEngine {

    private Logger LOG = Logger.getLogger(HypergeometricEnrichmentEngine.class);

    long totalctime = 0;
    long totalptime = 0;
    
    BMKnowledgeBase kb;

    // caches
    List<Integer> numHypothesesByFilter = new ArrayList<>();
    Integer numHypothesesGlobal;

    protected EnrichmentConfig enrichmentConfig = new EnrichmentConfig();

    @Inject
    public HypergeometricEnrichmentEngine(BMKnowledgeBase kb) {
        super();
        this.kb = kb;
    }

    @Override
    public String getShortName() {
        return "hypergeometric";
    }
    
    

    /**
     * @return the enrichmentConfig
     */
    public EnrichmentConfig getEnrichmentConfig() {
        return enrichmentConfig;
    }

    /**
     * @param enrichmentConfig the enrichmentConfig to set
     */
    public void setEnrichmentConfig(EnrichmentConfig enrichmentConfig) {
        this.enrichmentConfig = enrichmentConfig;
    }

    @Override
    public void precompute() {
    }
    
    

    /**
     * @return the kb
     */
    public BMKnowledgeBase getKnowledgeBase() {
        return kb;
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
        LOG.info("  1st time calculating NumHypothesis for "+tid);

        // for bonferoni correction, each class with >1 individual in the filter
        // counts as a distinct hypothesis
        EWAHCompressedBitmap filteredIndividualsBM = tid == null ? null : kb.getIndividualsBM(tid);
        int n=0;
        for (String cid : kb.getClassIdsInSignature()) {
            LOG.info("CID="+cid);
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
        LOG.info("  Done calculating NumHypothesis for "+tid+" = "+n);

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
            
            // e.g. a taxon class
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
        long t1 = System.currentTimeMillis();

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
        
        // TODO - make configurable
        // while it is possible to get a statistically significant result
        // with only one sample member with the property, this should
        // be corrected after testing for multiple hypotheses
        if (enrichmentConfig.analysisType == AnalysisType.OVER && numShared < 2) {
            return null;
        }
        
        // TODO - make configurable
        if (enrichmentConfig.analysisType == AnalysisType.UNDER && numShared > 0) {
            return null;
        }
        
        int enrichedCardinality = enrichedBM.cardinality();
        int sampleCardinality = sampleBM.cardinality();
        
        // TODO - make configurable
        if (enrichmentConfig.analysisType == AnalysisType.UNDER && 
                (enrichedCardinality < 5 || sampleCardinality < 5)) {
            return null;
        }
        long t2 = System.currentTimeMillis();
 
        HypergeometricDistribution hg = new HypergeometricDistribution(
                popSize, enrichedCardinality, sampleCardinality);
        int min = Math.min(enrichedCardinality, sampleCardinality);
        //double p = hg.cumulativeProbability(numShared, min);
        double p = hg.upperCumulativeProbability(numShared);
        double pCorrected = p * numHypotheses;
        double pUnderRepresented = hg.cumulativeProbability(numShared);
        double pUnderRepresentedCorrected = pUnderRepresented * numHypotheses;

        long t3 = System.currentTimeMillis();
        
        long ctime = (t2-t1);
        long ptime = (t3-t2);
        totalctime += ctime;
        totalptime += ptime;
        if (new Random().nextInt(1000) == 0) {
            LOG.info("TOTAL TIME: c="+totalctime +" p="+totalptime);
        }
        

        return EnrichmentResultImpl.create(p, 
                pCorrected,
                pUnderRepresentedCorrected,
                enrichedClassId,
                numShared,
                enrichedCardinality,
                sampleCardinality
                );
    }

    @Override
    public EnrichmentResultSet calculateEnrichmentAgainstKb(
            EnrichmentQuery query) {
        return calculateEnrichmentAgainstKb(query, kb.getClassIdsInSignature());
    }
    
    @Override
    public EnrichmentResultSet calculateEnrichmentAgainstKb(
            EnrichmentQuery query, Set<String> targetClassIds) {

        List<EnrichmentResult> results = new ArrayList<>();
        for (String cid : targetClassIds) {
            EnrichmentResult r = calculateEnrichmentAgainstClass(query, cid);
            if (enrichmentConfig.overThreshold != null &&
                    r.getpCorrected() > enrichmentConfig.overThreshold) {
                continue;
            }
            if (enrichmentConfig.underThreshold != null && 
                    r.getpUnderRepresentedCorrected() > enrichmentConfig.underThreshold) {
                continue;
            }
            if (r != null)
                results.add(r);
        }
        EnrichmentResultSet rs = new EnrichmentResultSetImpl(query, results);
        
        // important: filtering requires first sorting by score
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
        rs = new EnrichmentResultSetImpl(query, nrresults);
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
        EnrichmentResultSetImpl rs = new EnrichmentResultSetImpl(query, results);
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
        LOG.debug("QUERY: "+q);
        if (q.getClassId() != null)
            return kb.getIndividualsBM(q.getClassId());
        else if (q.getIndividualIds() != null) {
            Set<Integer> ixs = q.getIndividualIds().stream().map( x -> kb.getIndividualIndex(x)).collect(Collectors.toSet());
            return EWAHUtils.convertIndexSetToBitmap(ixs);
        }
        else {
            return null;
        }
    }

    public static EnrichmentEngine create(BMKnowledgeBase kb) {
        return new HypergeometricEnrichmentEngine(kb);
    }



}
