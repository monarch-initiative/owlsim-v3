package org.monarchinitiative.owlsim.compute.matcher;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.GMProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaivesBayesFixedWeightProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ProfileMatcherWithNegationSpeciesTest extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(ProfileMatcherWithNegationSpeciesTest.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return NaivesBayesFixedWeightProfileMatcher.create(kb);
	}

	@Test
	public void testBasic() throws OWLOntologyCreationException, NonUniqueLabelException, FileNotFoundException, UnknownFilterException {
		loadSpecies();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		TestQuery tq = eval.constructTestQuery(labelMapper,
				"human",
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
		LOG.getRootLogger().setLevel(level);
		LOG.info("TQ="+tq.query);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
		
	}

	private void loadSpecies() throws OWLOntologyCreationException {
		load("/speciesWithNegation.owl");
		
	}

}
