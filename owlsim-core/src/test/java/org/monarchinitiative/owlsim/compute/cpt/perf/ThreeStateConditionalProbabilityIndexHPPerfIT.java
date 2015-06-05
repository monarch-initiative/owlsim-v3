package org.monarchinitiative.owlsim.compute.cpt.perf;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.cpt.AbstractThreeStateConditionalProbabilityIndexTest;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.cpt.impl.ThreeStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests performance of CPT
 * 
 * TODO: this tests 3-state CPT on data with no negation...
 * 
 * @author cjm
 *
 */
public class ThreeStateConditionalProbabilityIndexHPPerfIT extends AbstractThreeStateConditionalProbabilityIndexTest  {

	private Logger LOG = Logger.getLogger(ThreeStateConditionalProbabilityIndexHPPerfIT.class);

	/**
	 * Load ontology plus data ontologies
	 * 
	 * @param fn
	 * @param datafns
	 * @throws OWLOntologyCreationException
	 * @throws IncoherentStateException 
	 */
	protected void load(String fn, String... datafns) throws OWLOntologyCreationException, IncoherentStateException {
		OWLLoader loader = new OWLLoader();
		LOG.info("R="+fn);
		loader.load(getClass().getResource(fn).getFile());
		for (String datafn : datafns) {
			LOG.info("R="+datafn);
			URL res = getClass().getResource(datafn);
			LOG.info("RES="+res);
			loader.loadOntologies(res.getFile());
		}
		kb = loader.createKnowledgeBaseInterface();
		cpi = ThreeStateConditionalProbabilityIndex.create(kb);
		cpi.calculateConditionalProbabilities(kb);
	}

	@Test
	public void cptTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException, IncoherentStateException {
		loadHP();
		
		// we don't actually test any output: this is primarily for performance
		String[] expectedMatches = {};
		examineCPT(expectedMatches , 0);

	}

	private void loadHP() throws OWLOntologyCreationException, IncoherentStateException {
		load("/ontologies/hp.obo", "/data/Homo_sapiens-data.owl");				
	}
	



}
