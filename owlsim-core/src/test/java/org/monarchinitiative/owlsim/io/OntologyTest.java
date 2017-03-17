package org.monarchinitiative.owlsim.io;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class OntologyTest {

    private Map<String, String> getHpAndNameCurieMap() {
        Map<String, String> curies = new HashMap<>();
        curies.put("HP", "http://purl.obolibrary.org/obo/HP_");
        curies.put("MINION", "http://despicableme.wikia.com/wiki/");
        return curies;
    }

    private Ontology getBobOnlyOntology() {
        Map<String, String> curies = getHpAndNameCurieMap();

        Map<String, Set<String>> data = new HashMap<>();
        data.put("MINION:Bob", toSet("HP:0000952;HP:0001090;HP:0008857;HP:0001006;HP:0006101;HP:0001100"));

        OntologySourceData sourceData = OntologySourceData.builder()
                .ontology("src/test/resources/species-no-individuals.owl")
                .curies(curies)
                .individualAssociations(data)
                .build();

        return Ontology.load(sourceData);
    }

    private Set<String> toSet(String input) {
        return Arrays.stream(input.split("[,;]")).map(String::trim).collect(ImmutableSet.toImmutableSet());
    }

    private Set<OWLIndividualAxiom> getAxiomsForIndividual(Ontology ontology, String individual) {
        OWLOntology owlOntology = ontology.getOwlOntology();
        OWLOntologyManager manager = owlOntology.getOWLOntologyManager();
        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        OWLNamedIndividual owlNamedIndividual = owlDataFactory.getOWLNamedIndividual(ontology.toIri(individual));
        return owlOntology.getAxioms(owlNamedIndividual, Imports.INCLUDED);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullIndividuals() {
        Map<String, String> curies = getHpAndNameCurieMap();

        Map<String, Set<String>> data = new HashMap<>();
        data.put(null, toSet("HP:0000952,HP:0001090,HP:0004322,HP:0001006,HP:0006101"));

        OntologySourceData sourceData = OntologySourceData.builder()
                .ontology("src/test/resources/species-no-individuals.owl")
                .curies(curies)
                .individualAssociations(data)
                .build();

        Ontology.load(sourceData);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullClasses() {
        Map<String, String> curies = getHpAndNameCurieMap();

        Map<String, Set<String>> data = new HashMap<>();
        data.put("MINION:Kevin", null);

        OntologySourceData sourceData = OntologySourceData.builder()
                .ontology("src/test/resources/species-no-individuals.owl")
                .curies(curies)
                .individualAssociations(data)
                .build();

        Ontology.load(sourceData);
    }

    @Test
    public void testAddIndividuals() {
        Map<String, String> curies = getHpAndNameCurieMap();

        Map<String, Set<String>> data = new HashMap<>();
        //Concatenated - should be able to parse TSV, CSV and trim whitespace
        data.put("MINION:Kevin", toSet("HP:0000952,HP:0001090,HP:0004322,HP:0001006,HP:0006101"));
        data.put("MINION:Bob", toSet("HP:0000952;HP:0001090;HP:0008857;HP:0001006;HP:0006101;HP:0001100"));
        //mixed
        data.put("MINION:Stuart", toSet("HP:0000952; HP:0001090;HP:0008857 ;HP:0001006, HP:0006101,HP:0100754, HP:0009914  "));

        OntologySourceData sourceData = OntologySourceData.builder()
                .ontology("src/test/resources/species-no-individuals.owl")
                .curies(curies)
                .individualAssociations(data)
                .build();

        Ontology ontology = Ontology.load(sourceData);

        Set<OWLIndividualAxiom> kevinAxioms = getAxiomsForIndividual(ontology, "MINION:Kevin");
        kevinAxioms.forEach(axiom -> {
            Set<OWLClass> classes = axiom.getClassesInSignature();
            classes.forEach(ontologyClass -> System.out.printf("Individual: %s Class: %s%n", axiom.getIndividualsInSignature(), ontologyClass));
        });

        assertEquals(5, kevinAxioms.size());
        Set<OWLIndividualAxiom> bobAxioms = getAxiomsForIndividual(ontology, "MINION:Bob");
        assertEquals(6, bobAxioms.size());
        Set<OWLIndividualAxiom> stuartAxioms = getAxiomsForIndividual(ontology, "MINION:Stuart");
        assertEquals(7, stuartAxioms.size());
    }

    @Test
    public void testIriConversion() {
        Map<String, String> curies = getHpAndNameCurieMap();

        OntologySourceData sourceData = OntologySourceData.builder()
                .ontology("src/test/resources/species-no-individuals.owl")
                .curies(curies)
                .build();

        Ontology ontology = Ontology.load(sourceData);

        IRI bobNotFound = ontology.toIri("Bob");
        System.out.println(bobNotFound);
        assertEquals("Bob", bobNotFound.toString());
        assertEquals(ontology.toCurie(bobNotFound), "Bob");

        IRI bobFound = ontology.toIri("MINION:Bob");
        System.out.println(bobFound);
        assertEquals("http://despicableme.wikia.com/wiki/Bob", bobFound.toString());
        assertEquals(ontology.toCurie(bobFound), "MINION:Bob");

    }

    @Test
    public void testGetOwlClass() {
        Ontology ontology = getBobOnlyOntology();

        OWLClass hpClass = ontology.getOWLClass("HP:0000952");
        System.out.println(hpClass);
        assertEquals(hpClass.toString(), "<http://purl.obolibrary.org/obo/HP_0000952>");

        OWLClass bobFound = ontology.getOWLClass("MINION:Bob");
        System.out.println(bobFound);
        assertEquals(bobFound.toString(), "<http://despicableme.wikia.com/wiki/Bob>");
    }

    @Test
    public void testGetOwlNamedIndividual() {
        Ontology ontology = getBobOnlyOntology();

        OWLNamedIndividual notFound = ontology.getOWLNamedIndividual("wibble");
        System.out.println(notFound);
        assertEquals(notFound.toString(), "<wibble>");

        OWLNamedIndividual hpTerm = ontology.getOWLNamedIndividual("HP:0000952");
        System.out.println(hpTerm);
        assertEquals(hpTerm.toString(), "<http://purl.obolibrary.org/obo/HP_0000952>");

        OWLNamedIndividual bob = ontology.getOWLNamedIndividual("MINION:Bob");
        System.out.println(bob);
        assertEquals(bob.toString(), "<http://despicableme.wikia.com/wiki/Bob>");
    }

}