package org.monarchinitiative.owlsim.compute.matcher.perf;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.*;
import org.monarchinitiative.owlsim.eval.ProfileMatchEvaluator;
import org.monarchinitiative.owlsim.io.OwlKnowledgeBase;
import org.monarchinitiative.owlsim.io.ReadMappingsUtil;
import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.kb.filter.TypeFilter;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.util.*;

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
        Map<String, String> curies = new LinkedHashMap<>();
        curies.put("HP", "http://purl.obolibrary.org/obo/HP_");
        curies.put("MP", "http://purl.obolibrary.org/obo/MP_");
        curies.put("NCBITaxon", "http://purl.obolibrary.org/obo/NCBITaxon_");

        kb = OwlKnowledgeBase.loader()
                .loadCuries(curies)
                .loadOntology("src/test/resources/ontologies/mammal.obo.gz")
                .loadIndividualAssociationsFromTsv(
                        "src/test/resources/data/gene2taxon.tsv.gz",
                        "src/test/resources/data/mouse-pheno.assocs.gz",
                        "src/test/resources/data/human-pheno.assocs.gz")
                .createKnowledgeBase();

    }

}
