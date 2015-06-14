package org.monarchinitiative.owlsim.compute.matcher.mp;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class BayesianNetworkProfileMatcherMPTest extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(BayesianNetworkProfileMatcherMPTest.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return BayesianNetworkProfileMatcher.create(kb);
	}

	@Test
	public void testBasic() throws Exception {
		load();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		TestQuery tq = eval.constructTestQuery(labelMapper,
				"Epilepsy (fake for testing)",
				2,
				"nervous system phenotype",    // ep
				"abnormal synaptic transmission",
				//"reproductive system phenotype",   // 
				"abnormal cerebellum development"  // 
				);
		Level level = Level.DEBUG;
		LOG.setLevel(level );
		Logger.getRootLogger().setLevel(level);
		LOG.info("TQ="+tq.query);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
		
	}

	private void load() throws OWLOntologyCreationException {
		load("/mp-subset.ttl");
		
	}

}
