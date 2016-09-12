package org.monarchinitiative.owlsim.compute.enrich.impl;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentEngine;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentQuery;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResult;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResultSet;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class HypergeometricEnrichmentEngineTest {

    private Logger LOG = Logger.getLogger(HypergeometricEnrichmentEngineTest.class);

    BMKnowledgeBase kb;

    @Test
    public void test() throws OWLOntologyCreationException {
        load("simple-pheno-with-negation.owl");
        EnrichmentEngine ee = HypergeometricEnrichmentEngine.create(kb);

        for (String cid : kb.getClassIdsInSignature()) {
            LOG.info("CLASS: "+cid);
            EnrichmentQuery query = EnrichmentQueryImpl.create(cid);
            EnrichmentResultSet rs = ee.calculateEnrichmentAgainstKb(query);
            if (rs.getResults().size() == 0) {
                LOG.error("NO RESULTS");
                continue;
            }
            EnrichmentResult tr = rs.getResults().get(0);
            //LOG.info(tr + " SCORE: "+tr.getScore());
            for (EnrichmentResult r : rs.getResults()) {
                //LOG.info(r + " SCORE: "+r.getScore());
                System.out.println(r + " SCORE: "+r.getScore());
            }
        }
    }

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

}
