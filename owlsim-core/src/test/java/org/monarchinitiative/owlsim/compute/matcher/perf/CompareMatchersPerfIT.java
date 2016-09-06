package org.monarchinitiative.owlsim.compute.matcher.perf;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.JaccardSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.MaximumInformationContentSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightTwoStateProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.eval.ProfileMatchEvaluator;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * 
 * 
 * @author cjm
 *
 */
public class CompareMatchersPerfIT extends AbstractProfileMatcherTest {

    private Logger LOG = Logger.getLogger(CompareMatchersPerfIT.class);



    /**
     * Compares a fixed set of matchers
     */
    @Test
    public void testCompare() throws Exception {
        load();
        int numInds = kb.getIndividualIdsInSignature().size();
        LOG.info("NumInds = "+numInds);
        assertTrue(numInds > 0);

        Set<ProfileMatcher> pms = new HashSet<>();
        pms.add(NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb));
        //pms.add(NaiveBayesFixedWeightThreeStateProfileMatcher.create(kb));
        pms.add(BayesianNetworkProfileMatcher.create(kb));
        pms.add(PhenodigmICProfileMatcher.create(kb));
        pms.add(MaximumInformationContentSimilarityProfileMatcher.create(kb));
        pms.add(JaccardSimilarityProfileMatcher.create(kb));
        compare(pms, 5);


    }

    /**
     * Compare two BN methods
     * 
     * @throws Exception
     */
    @Test
    public void testCompareBN() throws Exception {
        load();
        int numInds = kb.getIndividualIdsInSignature().size();
        LOG.info("NumInds = "+numInds);
        assertTrue(numInds > 0);

        Set<ProfileMatcher> pms = new HashSet<>();
        pms.add(NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb));
        pms.add(BayesianNetworkProfileMatcher.create(kb));
        compare(pms, 15);


    }

    /**
     * 
     * 
     * @param pms
     * @param N - num iterations
     * @throws UnknownFilterException
     * @throws IncoherentStateException
     * @throws FileNotFoundException
     */
    private void compare(Set<ProfileMatcher> pms, int N) throws UnknownFilterException, IncoherentStateException, FileNotFoundException {
        ProfileMatchEvaluator pme = new ProfileMatchEvaluator();
        LOG.setLevel(Level.OFF);
        LOG.getRootLogger().setLevel(Level.WARN);
        for (int i=0; i<N; i++) {
            pme.runNoiseSimulation(kb, pms, "target");
        }
    }



    private void load() throws OWLOntologyCreationException {
        load("/ontologies/hp.obo", "/data/Homo_sapiens-data.owl");		
    }

}
