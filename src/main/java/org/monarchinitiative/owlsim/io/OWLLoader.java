package org.monarchinitiative.owlsim.io;

import java.io.File;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * Object for loading OWL ontologies into a {@link BMKnowledgeBase}
 * 
 * @author cjm
 *
 */
public class OWLLoader {
	
	OWLOntologyManager manager;
	OWLOntology owlOntology;
	OWLReasoner owlReasoner;
	OWLReasonerFactory owlReasonerFactory = new ElkReasonerFactory();
	
	
	/**
	 * @param iri
	 * @throws OWLOntologyCreationException
	 */
	public void load(IRI iri) throws OWLOntologyCreationException {
		owlOntology = getOWLOntologyManager().loadOntology(iri);
	}
	
	/**
	 * @param file
	 * @throws OWLOntologyCreationException
	 */
	public void load(File file) throws OWLOntologyCreationException {
	    IRI iri = IRI.create(file);
	    owlOntology =  getOWLOntologyManager().loadOntologyFromOntologyDocument(iri);
	    
	}

	/**
	 * Loads an OWL ontology from a URI or file
	 * 
	 * @param path
	 * @throws OWLOntologyCreationException
	 */
	public void load(String path) throws OWLOntologyCreationException {
		if (path.startsWith("http")) {
			 load(IRI.create(path));
		}
		else {
			File file = new File(path);
			load(file);
		}
	}
	
	private OWLOntologyManager getOWLOntologyManager() {
		if (manager == null)
			manager = OWLManager.createOWLOntologyManager();
		return manager;
	}
	
	/**
	 * @return handle for a Bitmap-based Knowledge Base
	 */
	public BMKnowledgeBase createKnowledgeBaseInterface() {
		// TODO: use factories, or injection
		if (owlReasoner == null) {
			owlReasoner = owlReasonerFactory.createReasoner(owlOntology);
		}
		
		return new BMKnowledgeBaseOWLAPIImpl(owlOntology, owlReasoner);
	}
}
