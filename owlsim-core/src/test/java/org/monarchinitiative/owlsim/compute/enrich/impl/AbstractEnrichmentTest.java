package org.monarchinitiative.owlsim.compute.enrich.impl;

import java.net.URL;

import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class AbstractEnrichmentTest {

    protected BMKnowledgeBase kb;

    /**
     * Load an ontology from resources folder
     * 
     * @param fn
     * @throws OWLOntologyCreationException
     */
    protected void load(String fn) throws OWLOntologyCreationException {
        OWLLoader loader = new OWLLoader();
        loader.load("src/test/resources/"+fn);
        kb = loader.createKnowledgeBaseInterface();
    }
    
    /**
     * Load ontology plus data ontologies
     * 
     * @param fn
     * @param ontfns
     * @throws OWLOntologyCreationException
     */
    protected void load(String fn, String... ontfns) throws OWLOntologyCreationException {
        OWLLoader loader = new OWLLoader();
        loader.load(getClass().getResource(fn).getFile());
        for (String ontfn : ontfns) {
            URL res = getClass().getResource(ontfn);
            loader.loadOntologies(res.getFile());
        }
        kb = loader.createKnowledgeBaseInterface();
    }


}
