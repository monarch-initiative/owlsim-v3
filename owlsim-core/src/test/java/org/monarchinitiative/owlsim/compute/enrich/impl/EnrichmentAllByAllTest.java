package org.monarchinitiative.owlsim.compute.enrich.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentEngine;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentQuery;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResult;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResultSet;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class EnrichmentAllByAllTest extends AbstractEnrichmentTest {

    private Logger LOG = Logger.getLogger(EnrichmentAllByAllTest.class);

 
    @Test
    public void test() throws OWLOntologyCreationException {
        load("simple-pheno-with-negation.owl");
        EnrichmentEngine ee = HypergeometricEnrichmentEngine.create(kb);

        String root = "http://x.org/phenotype";
        EnrichmentAllByAll axa = new EnrichmentAllByAll();
        List<EnrichmentResultSet> rsl = axa.getAllByAll(ee, root, root);
        for (EnrichmentResultSet rs : rsl) {
            LOG.info("RS="+rs);
        }
        
  
    }
    
    @Test
    public void testMP() throws OWLOntologyCreationException {
        load("mp-subset.ttl");
        EnrichmentEngine ee = HypergeometricEnrichmentEngine.create(kb);

        String root = "http://purl.obolibrary.org/obo/MP_0000001";
        EnrichmentAllByAll axa = new EnrichmentAllByAll();
        List<EnrichmentResultSet> rsl = axa.getAllByAll(ee, root, root);
        for (EnrichmentResultSet rs : rsl) {
            LOG.info("RS="+rs);
        }
        
  
    }

}
