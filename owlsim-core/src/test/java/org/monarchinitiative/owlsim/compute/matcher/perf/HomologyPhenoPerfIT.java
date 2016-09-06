package org.monarchinitiative.owlsim.compute.matcher.perf;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.JaccardSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.MaximumInformationContentSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightTwoStateProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.eval.ProfileMatchEvaluator;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.io.ReadMappingsUtil;
import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.kb.filter.TypeFilter;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests phenotype matcher by finding rank of each homolog when using that matcher
 * 
 * src/test/resources/data/mammal-homol.tsv contains list of human-mouse
 * homology pairs
 * 
 * Use human as query, test rank of ortholog
 * 
 * @author cjm
 *
 */
public class HomologyPhenoPerfIT extends AbstractProfileMatcherTest {

    private Logger LOG = Logger.getLogger(HomologyPhenoPerfIT.class);



    @Test
    public void testHomologyBN() throws Exception {
        load();
        Set<ProfileMatcher> pms = new HashSet<>();
        pms.add(NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb));
        pms.add(BayesianNetworkProfileMatcher.create(kb));
        testHomology(pms);
    }

    /**
     * Note that this test can take a few hours, it attempts uses
     * all homology statements as test test
     * 
     * @throws Exception
     */
    @Test
    public void testHomologyMulti() throws Exception {
        load();
        Set<ProfileMatcher> pms = new HashSet<>();
        pms.add(NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb));
        pms.add(BayesianNetworkProfileMatcher.create(kb));
        pms.add(PhenodigmICProfileMatcher.create(kb));
        pms.add(MaximumInformationContentSimilarityProfileMatcher.create(kb));
        pms.add(JaccardSimilarityProfileMatcher.create(kb));
        testHomology(pms);
    }


    /**
     * TODO: DRY - move this to eval
     */	
    public void testHomology(Set<ProfileMatcher> pms ) throws Exception {
        ProfileMatchEvaluator pme = new ProfileMatchEvaluator();
        Map<String, String> mappings = 
                ReadMappingsUtil.readPairwiseMappingsFromTsv(getClass().getResource("/data/mammal-homol.tsv").getPath());
        LOG.getRootLogger().setLevel(Level.WARN);
        pme.recapitulateHomologies(kb, pms, mappings, "target", getMouseFilter());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testHomologyQuick() throws Exception {
        load();
        Set<ProfileMatcher> pms = new HashSet<>();
        pms.add(NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb));
        pms.add(BayesianNetworkProfileMatcher.create(kb));
        pms.add(PhenodigmICProfileMatcher.create(kb));
        pms.add(MaximumInformationContentSimilarityProfileMatcher.create(kb));
        pms.add(JaccardSimilarityProfileMatcher.create(kb));
        ProfileMatchEvaluator pme = new ProfileMatchEvaluator();
        Map<String, String> mappings = new HashMap<>();
        //mappings.put("NKX2-1", "Nkx2-1");
        mappings.put("UNQ5828/PRO19647", "Gpn2");
        pme.recapitulateHomologies(kb, pms, mappings, "target", getMouseFilter());
    }
    
    //UNQ5828/PRO19647

    private Filter getMouseFilter() {
        return new TypeFilter("http://purl.obolibrary.org/obo/NCBITaxon_10090");
        //return new TypeFilter("NCBITaxon:10090");
    }


    @Test
    public void estimateAccuracy() throws Exception {
        load();
        Map<String, String> mappings = 
                ReadMappingsUtil.readPairwiseMappingsFromTsv(getClass().
                        getResource("/data/mammal-homol.tsv").getPath());
        NaiveBayesFixedWeightTwoStateProfileMatcher pm = NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb);
        for (String g1 : mappings.keySet()) {
            if (!kb.getIndividualIdsInSignature().contains(g1)) {
                continue;
            }
            String g2 = mappings.get(g1);
            if (!kb.getIndividualIdsInSignature().contains(g2)) {
                continue;
            }
            pm.compare(g1, g2);
        }
    }

    private void load() throws OWLOntologyCreationException, IOException {
        OWLLoader loader = new OWLLoader();
        loader.loadGzippdOntology(getClass().getResource("/ontologies/mammal.obo.gz").getFile());
        loader.loadDataFromTsvGzip(getClass().getResource("/data/gene2taxon.tsv.gz").getFile());
        loader.loadDataFromTsvGzip(getClass().getResource("/data/mouse-pheno.assocs.gz").getFile());
        loader.loadDataFromTsvGzip(getClass().getResource("/data/human-pheno.assocs.gz").getFile());
        kb = loader.createKnowledgeBaseInterface();

    }

}
