package org.monarchinitiative.owlsim.eval;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class RandomOntologyMaker {
	
	private Logger LOG = Logger.getLogger(RandomOntologyMaker.class);

	private int counter;
	private String prefix = "http://example.org/";
	private List<OWLClass> classList;
	private OWLOntology ontology;
	private OWLOntologyManager owlOntologyManager;
	
	
	public RandomOntologyMaker() {
		super();
		classList = new ArrayList<OWLClass>();
		counter = 0;
	}
	
	public static RandomOntologyMaker create(int numberOfClasses, double averageNumberOfParents) throws OWLOntologyCreationException {
		RandomOntologyMaker rom = new RandomOntologyMaker();
		return rom.createOntology(numberOfClasses, averageNumberOfParents);
	}

	public RandomOntologyMaker createOntology(int numberOfClasses, double averageNumberOfParents) throws OWLOntologyCreationException {
		owlOntologyManager = OWLManager.createOWLOntologyManager();
		ontology = owlOntologyManager.createOntology();
		while (counter < numberOfClasses) {
			OWLClass c = createClass();
			
			int i=0;
			while (true) {
				OWLClass parent = getRandomClass();
				OWLSubClassOfAxiom ax = getOWLDataFactory().getOWLSubClassOfAxiom(c, parent);
				addAxiom(ax);
				i++;
				if (Math.random() < (1 / (double) averageNumberOfParents) ) {
					break;
				}
			}
		}
		return this;
	}
	
	public RandomOntologyMaker addRandomIndividuals(int numIndividuals) {
		return addRandomIndividuals(numIndividuals, 5);
	}
	
	public RandomOntologyMaker addRandomIndividuals(int numIndividuals, int avgTypes) {
		for (int i=0; i<numIndividuals; i++) {
			OWLIndividual individual = createIndividual(i);
			//LOG.info("INDIVIDUAL:"+individual);
			//add at least one
			addRandomClassToIndividual(individual);
			while (Math.random() > (1 / (double) avgTypes)) {
				addRandomClassToIndividual(individual);
			}
		}
		return this;
	}
	
	private void addRandomClassToIndividual(OWLIndividual individual) {
		OWLClass parent = getRandomClass();
		addAxiom(getOWLDataFactory().getOWLClassAssertionAxiom(parent, individual));
	}
	
	public RandomOntologyMaker addEquivalentClasses(int numECs) {
		for (int i=0; i<numECs; i++) {
			OWLClass c = createClass();
			OWLClass e = getRandomClass();
			addAxiom(getOWLDataFactory().getOWLEquivalentClassesAxiom(c, e));
		}
		return this;
	}

	
	private void addAxiom(OWLAxiom ax) {
		//LOG.info("ADDING: "+ax);
		ontology.getOWLOntologyManager().addAxiom(ontology, ax);
	}

	
	private OWLClass getRandomClass() {
		if (counter == 0) {
			return getOWLDataFactory().getOWLThing();
		}
		else {
			int n = (int) (Math.random() * counter);
			return classList.get(n);
		}
	}

	private IRI createIRI(int n) {
		IRI iri = IRI.create(prefix + n);
		return iri;
	}

	private OWLClass createClass() {
		counter++;
		IRI iri = createIRI(counter);
		OWLClass c = getOWLDataFactory().getOWLClass(iri);
		classList.add(c);
		return c;
	}
	
	private OWLNamedIndividual createIndividual(int n) {
		IRI iri = createIRI(n);
		OWLNamedIndividual ind = getOWLDataFactory().getOWLNamedIndividual(iri);
		return ind;
	}
	
	private OWLDataFactory getOWLDataFactory() {
		return owlOntologyManager.getOWLDataFactory();
	}
	
	public OWLOntology getOntology() {
		return this.ontology;
	}
}
