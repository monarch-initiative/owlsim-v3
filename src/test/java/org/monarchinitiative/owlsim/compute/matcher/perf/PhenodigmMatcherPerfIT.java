package org.monarchinitiative.owlsim.compute.matcher.perf;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Performance test for PhenodigmICProfileMatcher using full HPO and HPO annotations,
 * queried using a single profile
 * 
 * This test relies on files being present in resources - see the Makefile for details
 * 
 * @author cjm
 *
 */
public class PhenodigmMatcherPerfIT extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(PhenodigmMatcherPerfIT.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return PhenodigmICProfileMatcher.create(kb);
	}

	@Test
	public void testBasic() throws OWLOntologyCreationException, NonUniqueLabelException, FileNotFoundException, UnknownFilterException {
		load();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/phenodigm-results.json");
		TestQuery tq = eval.constructTestQuery(labelMapper,
				"Renal Dysplasia - Megalocystis - Sirenomelia",
				6,
				"Scrotal hypoplasia",
				"Renal cyst",
				"Micrognathia");		Level level = Level.DEBUG;
		LOG.setLevel(level );
		LOG.getRootLogger().setLevel(level);
		LOG.info("TQ="+tq.query);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
		
	}

	private void load() throws OWLOntologyCreationException {
		load("/ontologies/hp.obo", "/data/omim-disease-phenotype.owl", "/data/disorders.ttl");		
	}

}
