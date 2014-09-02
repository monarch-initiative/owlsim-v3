package org.monarchinitiative.owlsim.io;

import java.io.File;

import org.apache.log4j.Logger;
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

import com.google.common.base.Preconditions;

/**
 * Object for loading OWL ontologies into a {@link BMKnowledgeBase}
 * 
 * @author cjm
 *
 */
public class OWLLoader {
	private Logger LOG = Logger.getLogger(OWLLoader.class);

	OWLOntologyManager manager;
	OWLOntology owlOntology;
	OWLOntology owlDataOntology;
	OWLReasoner owlReasoner;
	OWLReasonerFactory owlReasonerFactory = new ElkReasonerFactory();
	
	/**
	 * @param iri
	 * @return OWL Ontology 
	 * @throws OWLOntologyCreationException
	 */
	public OWLOntology loadOWL(IRI iri) throws OWLOntologyCreationException {
		return getOWLOntologyManager().loadOntology(iri);
	}
	
	/**
	 * @param file
	 * @return OWL Ontology
	 * @throws OWLOntologyCreationException
	 */
	public OWLOntology loadOWL(File file) throws OWLOntologyCreationException {
	    IRI iri = IRI.create(file);
	    return getOWLOntologyManager().loadOntologyFromOntologyDocument(iri);	    
	}

	/**
	 * Loads an OWL ontology from a URI or file
	 * 
	 * @param path
	 * @return OWL Ontology
	 * @throws OWLOntologyCreationException
	 */
	public OWLOntology loadOWL(String path) throws OWLOntologyCreationException {
		if (path.startsWith("http")) {
			 return loadOWL(IRI.create(path));
		}
		else {
			File file = new File(path);
			return loadOWL(file);
		}
	}
	
	/**
	 * @param iri
	 * @throws OWLOntologyCreationException
	 */
	public void load(IRI iri) throws OWLOntologyCreationException {
		owlOntology = getOWLOntologyManager().loadOntology(iri);
		Preconditions.checkNotNull(owlOntology);	    
	}
	
	/**
	 * @param file
	 * @throws OWLOntologyCreationException
	 */
	public void load(File file) throws OWLOntologyCreationException {
		owlOntology = loadOWL(file);
		Preconditions.checkNotNull(owlOntology);	    
	}
	
	

	/**
	 * Loads an OWL ontology from a URI or file
	 * 
	 * @param path
	 * @throws OWLOntologyCreationException
	 */
	public void load(String path) throws OWLOntologyCreationException {
		owlOntology = loadOWL(path);
		Preconditions.checkNotNull(owlOntology);	    
	}

	/**
	 * Loads an OWL ontology from a URI or file
	 * 
	 * @param path
	 * @throws OWLOntologyCreationException
	 */
	public void loadData(String... paths) throws OWLOntologyCreationException {
		for (String path : paths)
			mergeData( loadOWL(path) );
		Preconditions.checkNotNull(owlDataOntology);	    
	}
	
	private void mergeData(OWLOntology o) {
		if (owlDataOntology == null) {
			LOG.info("Data ontology="+o);
			owlDataOntology = o;
		}
		else {
			LOG.info("Merging data axioms from="+o);
			owlDataOntology.getOWLOntologyManager().addAxioms(owlDataOntology, o.getAxioms());
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
		
		return BMKnowledgeBaseOWLAPIImpl.create(owlOntology, owlDataOntology, owlReasonerFactory);
	}
}
