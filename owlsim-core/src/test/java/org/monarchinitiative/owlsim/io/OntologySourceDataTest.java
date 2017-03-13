package org.monarchinitiative.owlsim.io;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class OntologySourceDataTest {

    @Test
    public void testItAll() {

        Map<String, String> curies = new LinkedHashMap<>();
        curies.put("HP", "http://purl.obolibrary.org/obo/HP_");
        curies.put("MP", "http://purl.obolibrary.org/obo/MP_");
        curies.put("NCBITaxon", "http://purl.obolibrary.org/obo/NCBITaxon_");

        OntologySourceData sourceData = OntologySourceData.builder()
                .curies(curies)
                .ontology("src/test/resources/ontologies/mammal.obo.gz")
                .dataTsv("src/test/resources/data/gene2taxon.tsv.gz")
                .dataTsv("src/test/resources/data/mouse-pheno.assocs.gz")
                .dataTsv("src/test/resources/data/human-pheno.assocs.gz")
                .build();

        System.out.println(sourceData);
    }

    @Test(expected = Exception.class)
    public void testThrowsExceptionWhenCuriesEmptyAndDataIncludedFromTsv() {

        OntologySourceData sourceData = OntologySourceData.builder()
                .dataTsv("src/test/resources/data/gene2taxon.tsv.gz")
                .build();

        System.out.println(sourceData);
    }

}