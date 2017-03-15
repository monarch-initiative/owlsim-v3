package org.monarchinitiative.owlsim.io;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
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

        Map<String, List<String>> individuals = new LinkedHashMap<>();
        individuals.put("ORPHA:710", Arrays.asList("HP:0000194",
                "HP:0000218",
                "HP:0000262",
                "HP:0000303",
                "HP:0000316",
                "HP:0000322",
                "HP:0000324",
                "HP:0000348",
                "HP:0000431",
                "HP:0000470",
                "HP:0000508",
                "HP:0001156",
                "HP:0001385",
                "HP:0003307",
                "HP:0004209",
                "HP:0004322",
                "HP:0005048",
                "HP:0006101",
                "HP:0009773",
                "HP:0010669",
                "HP:0011304",
                "HP:0012368", ""));

        OntologySourceData sourceData = OntologySourceData.builder()
                .curies(curies)
                .ontology("src/test/resources/ontologies/mammal.obo.gz")
                .dataTsv("src/test/resources/data/gene2taxon.tsv.gz")
                .dataTsv("src/test/resources/data/mouse-pheno.assocs.gz")
                .dataTsv("src/test/resources/data/human-pheno.assocs.gz")
                .data(individuals)
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