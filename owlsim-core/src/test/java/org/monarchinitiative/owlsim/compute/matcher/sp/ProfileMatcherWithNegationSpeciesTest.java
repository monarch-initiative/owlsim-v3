package org.monarchinitiative.owlsim.compute.matcher.sp;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ProfileMatcherWithNegationSpeciesTest extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(ProfileMatcherWithNegationSpeciesTest.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return NaiveBayesFixedWeightProfileMatcher.create(kb);
	}

	@Test
	public void testBasic() throws Exception {
		loadSpecies();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/nst.json");
		TestQuery tq = eval.constructTestQuery(labelMapper,
				"OnlyInsect",
				10,
				//"human",
				"not deuterostome",
				"protostome",
				"fruitfly",
				"not spider",
				//"not mouse",
				//"not rodent",
				//"not cetacean",
				//"not carnivore",
				"not zebrafish");
		Level level = Level.DEBUG;
		LOG.setLevel(level );
		Logger.getRootLogger().setLevel(level);
		LOG.info("TQ="+tq.query);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
		
	}

	private void loadSpecies() throws OWLOntologyCreationException {
		load("/speciesWithNegation.owl");
		
	}

}
