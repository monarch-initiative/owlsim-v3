package org.monarchinitiative.owlsim.compute.matcher.perf;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesianNetworkProfileMatcher;
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
public class BayesianNetworkProfileMatcherPerfIT extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(BayesianNetworkProfileMatcherPerfIT.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return BayesianNetworkProfileMatcher.create(kb);
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
	public void testQueryWithSelf() throws Exception {
		load();
		Level level = Level.INFO;
		LOG.setLevel(level );
		Logger.getRootLogger().setLevel(level);
		int numInds = kb.getIndividualIdsInSignature().size();
		LOG.info("NumInds = "+numInds);
		assertTrue(numInds > 0);
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/bn-it-results-sjs.json");
		String d = "Schwartz-Jampel Syndrome, Type 1";
		TestQuery tq = eval.constructTestQueryAgainstIndividual(
				kb,
				labelMapper,
				d,
				1,
				d);		
		LOG.info("TQ="+tq.query);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));

	}
	
	@Test
	public void testQueryWithNegation() throws Exception {
		load();
		Level level = Level.INFO;
		LOG.setLevel(level );
		Logger.getRootLogger().setLevel(level);
		int numInds = kb.getIndividualIdsInSignature().size();
		LOG.info("NumInds = "+numInds);
		assertTrue(numInds > 0);
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/bn-it-results-neg-slo.json");
		TestQuery tq = eval.constructTestQuery(labelMapper,
				"Genitopatellar Syndrome",
				3,
				"Scrotal hypoplasia",
				"Renal cyst",
				"Micrognathia",
				"not Vomiting", 
				"not Abnormal lung lobation");		
		LOG.info("TQ="+tq.query);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));

	}

	@Test
	public void testSingleProfileQuery() throws Exception {
		load();
		Level level = Level.INFO;
		LOG.setLevel(level );
		Logger.getRootLogger().setLevel(level);
		int numInds = kb.getIndividualIdsInSignature().size();
		LOG.info("NumInds = "+numInds);
		assertTrue(numInds > 0);
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/bn-it-results-rdms.json");
		TestQuery tq = eval.constructTestQuery(labelMapper,
				"Smith-Lemli-Opitz Syndrome",
				16,
				"Scrotal hypoplasia",
				"Renal cyst",
				"Micrognathia");		
		LOG.info("TQ="+tq.query);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));

	}

	@Test
	public void testQueryMultiple() throws Exception {
		load();
		Level level = Level.INFO;
		LOG.setLevel(level );
		Logger.getRootLogger().setLevel(level);
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/bn-it-results-multi.json");
		String dq = "Schwartz-Jampel Syndrome, Type 1";
		assertTrue(eval.evaluateTestQuery(profileMatcher,
			     eval.constructTestQueryAgainstIndividual(
					kb,
					labelMapper,
					"Schwartz-Jampel Syndrome (Orphanet)",
					3,
					dq)));
		TestQuery tq = eval.constructTestQueryAgainstIndividual(
				kb,
				labelMapper,
				"Arthrogryposis, Distal, Type 2a",
				6,
				dq);
		tq.maxTimeMs = 10000;
		assertTrue(eval.evaluateTestQuery(profileMatcher,
			     tq));		
	}
	private void load() throws OWLOntologyCreationException {
		load("/ontologies/hp.obo", "/data/Homo_sapiens-data.owl");		
	}

}
