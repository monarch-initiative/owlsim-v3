package org.monarchinitiative.owlsim.io;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Utility class for creating a {@link BMKnowledgeBase} from input ontologies, curies and data. Ontologies can be in OWL
 * or OBO format, gzipped or uncompressed.
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class OwlKnowledgeBaseTest {

    private static final String SPECIES_OWL = "src/test/resources/species.owl";

    private Map<String, String> curies() {
        Map<String, String> curies = new LinkedHashMap<>();
        curies.put("HP", "http://purl.obolibrary.org/obo/HP_");
        curies.put("MP", "http://purl.obolibrary.org/obo/MP_");
        curies.put("NCBITaxon", "http://purl.obolibrary.org/obo/NCBITaxon_");
        return curies;
    }

    @Test
    public void testLoadOwlFromFile() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadOntology(new File(SPECIES_OWL))
                .createKnowledgeBase();
    }

    @Test
    public void testLoadOwlWithIndividualsFromFilePath() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadOntology(SPECIES_OWL)
                .createKnowledgeBase();
        assertEquals(24, bmKnowledgeBase.getIndividualIdsInSignature().size());
        //why -1? This is because http://www.w3.org/2002/07/owl#Thing is also reported as class.
        assertEquals(77, bmKnowledgeBase.getClassIdsInSignature().size() - 1);
    }

    @Test
    public void testLoadGzippedOboOntology() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadOntology("src/test/resources/ontologies/mammal.obo.gz")
                .createKnowledgeBase();
    }

    @Test
    public void testLoadOntologiesFromMultipleSources() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadOntologies(
                        Arrays.asList("src/test/resources/species.owl",
                        "http://purl.obolibrary.org/obo/aeo.owl",
                        "src/test/resources/ontologies/mammal.obo.gz")
                )
                .createKnowledgeBase();
    }

    /**
     * Ignored so as not to use network - this is a slow test
     */
//    @Ignore
    @Test
    public void testLoadRemoteOntology() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadOntology("http://purl.obolibrary.org/obo/aeo.owl")
                .createKnowledgeBase();
    }

    @Test(expected = OntologyLoadException.class)
    public void testLoadGzippedDataFileNoOntology() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadDataFromTsv("src/test/resources/data/human-pheno.assocs.gz")
                .createKnowledgeBase();
    }

    @Test
    public void loadDataFromOntology() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadOntology("src/test/resources/species.owl")
                .loadCuries(curies())
                .loadDataFromOntology("src/test/resources/species.owl")
                .createKnowledgeBase();
    }

    @Test
    public void loadDataFromOntologies() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadOntology("src/test/resources/ontologies/mammal.obo.gz")
                .loadCuries(curies())
                .loadDataFromOntologies("src/test/resources/mp-subset.ttl", "src/test/resources/mp-subset.ttl")
                .createKnowledgeBase();
    }

    @Test
    public void loadDataFromTsv() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadCuries(curies())
                .loadOntology("src/test/resources/species-no-individuals.owl")
                .loadDataFromTsv("src/test/resources/data/species-individuals.tsv")
                .createKnowledgeBase();
    }

    @Test
    public void loadDataFromGzippedTsv() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadCuries(curies())
                .loadOntology("src/test/resources/ontologies/mammal.obo.gz")
                .loadDataFromTsv("src/test/resources/data/human-pheno.assocs.gz")
                .createKnowledgeBase();
    }

    @Test
    public void loadDataFromTsvCollection() {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .loadCuries(curies())
                .loadOntology("src/test/resources/ontologies/mammal.obo.gz")
                .loadDataFromTsv(Arrays.asList(
                        "src/test/resources/data/gene2taxon.tsv.gz",
                        "src/test/resources/data/mouse-pheno.assocs.gz",
                        "src/test/resources/data/human-pheno.assocs.gz"))
                .createKnowledgeBase();

        //|classes|=38627
        //|individuals|=14200
        //What should this be? It's different from the OWLLoader version.
        //Turns out that without the correct curies the classes are not properly resolved so there will be 53630 classes
        // without any curies. About 40,000-odd without the MP curie and 38629 without the NCBITaxon curie (1 mouse, 1
        // human class).
        //So remember folks, curies are good, especially with poppadums and beer.

        //lastly, why -1? This is because http://www.w3.org/2002/07/owl#Thing is also reported as class.
        assertEquals(38627, bmKnowledgeBase.getClassIdsInSignature().size() - 1);
        assertEquals(14200, bmKnowledgeBase.getIndividualIdsInSignature().size());
    }

    @Test
    public void loadDataFromMap() {
        Map<String, String> curies = new HashMap<>();
        curies.put("HP", "http://purl.obolibrary.org/obo/HP_");
        curies.put("NAME:", "http://x.org/NAME_");

        Map<String, List<String>> data = new HashMap<>();
        data.put("NAME:Kevin", Arrays.asList("HP:0000952","HP:0001090","HP:0004322","HP:0001006","HP:0006101","HP:0009914"));
        data.put("NAME:Bob", Arrays.asList("HP:0000952","HP:0001090","HP:0004322","HP:0001006","HP:0006101","HP:0001100"));
        data.put("NAME:Stuart", Arrays.asList("HP:0000952","HP:0001090","HP:0004322","HP:0001006","HP:0006101","HP:0100754"));

        BMKnowledgeBase knowledgeBase = BMKnowledgeBase.owlLoader()
                .loadOntology("src/test/resources/species-no-individuals.owl")
                .loadCuries(curies)
                .loadDataFromMap(data)
                .createKnowledgeBase();

        System.out.println("knowledgebase individuals are: " + knowledgeBase.getIndividualIdsInSignature());
        System.out.println(knowledgeBase.getEntity("NAME:Kevin"));
        assertEquals(Sets.newHashSet("NAME:Kevin", "NAME:Bob", "NAME:Stuart"), knowledgeBase.getIndividualIdsInSignature());
    }

    @Test
    public void testLoadOwlFromFileLocationWithStandardOntologyManager() throws Exception {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .useStandardOntologyManager()
                .loadOntology(SPECIES_OWL)
                .createKnowledgeBase();
    }

    @Test
    public void canSpecifyConcurrentOntologyManager() {
        OwlKnowledgeBase.loader().useConcurrentOntologyManager();
    }

    @Test
    public void canSpecifyStandardOntologyManager() {
        OwlKnowledgeBase.loader().useStandardOntologyManager();
    }

    @Test
    public void testUseOtherOwlReasonerFactory() throws Exception {
        BMKnowledgeBase bmKnowledgeBase = OwlKnowledgeBase.loader()
                .useReasonerFactory(new JFactFactory())
                .loadOntology(SPECIES_OWL)
                .createKnowledgeBase();
    }
}