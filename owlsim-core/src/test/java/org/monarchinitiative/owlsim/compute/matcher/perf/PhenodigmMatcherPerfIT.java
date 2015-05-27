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
	public void testPhenodigmSingleProfileQuery() throws Exception {
		load();
		int numInds = kb.getIndividualIdsInSignature().size();
		LOG.info("NumInds = "+numInds);
		assertTrue(numInds > 0);
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/phenodigm-results.json");
		TestQuery tq = eval.constructTestQuery(labelMapper,
				"Androgen Insensitivity, Partial",
				8,
				"Scrotal hypoplasia",
				"Renal cyst",
				"Micrognathia");		
		Level level = Level.DEBUG;
		LOG.setLevel(level );
		Logger.getRootLogger().setLevel(level);
		LOG.info("TQ="+tq.query);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));

	}

	/**
	 * Tests that self is the top hit for Schwartz-Jampel Syndrome
	 * 
	 * @throws OWLOntologyCreationException
	 * @throws NonUniqueLabelException
	 * @throws FileNotFoundException
	 * @throws UnknownFilterException
	 */
	@Test
	public void testPhenodigmQueryWithSelf() throws Exception {
		load();
		int numInds = kb.getIndividualIdsInSignature().size();
		LOG.info("NumInds = "+numInds);
		assertTrue(numInds > 0);
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/phenodigm-it-results-sjs.json");
		String d = "Schwartz-Jampel Syndrome, Type 1";
		TestQuery tq = eval.constructTestQueryAgainstIndividual(
				kb,
				labelMapper,
				d,
				1,
				d);		
		Level level = Level.DEBUG;
		LOG.setLevel(level );
		Logger.getRootLogger().setLevel(level);
		LOG.info("TQ="+tq.query);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));

	}

	@Test
	public void testPhenodigmQueryMultiple() throws Exception {
		load();
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/phenodigm-it-results-multi.json");
		String dq = "Schwartz-Jampel Syndrome, Type 1";
		assertTrue(eval.evaluateTestQuery(profileMatcher,
			     eval.constructTestQueryAgainstIndividual(
					kb,
					labelMapper,
					"Microcephalic Osteodysplastic Primordial Dwarfism, Type I",
					3,
					dq)));		
		assertTrue(eval.evaluateTestQuery(profileMatcher,
			     eval.constructTestQueryAgainstIndividual(
					kb,
					labelMapper,
					"Multicentric Osteolysis, Nodulosis, And Arthropathy",
					6,
					dq)));		
	}
	private void load() throws OWLOntologyCreationException {
		load("/ontologies/hp.obo", "/data/Homo_sapiens-data.owl");		
	}

}
