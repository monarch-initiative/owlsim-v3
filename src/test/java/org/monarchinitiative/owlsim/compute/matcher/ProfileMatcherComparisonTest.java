package org.monarchinitiative.owlsim.compute.matcher;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.GridProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ProfileMatcherComparisonTest extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(ProfileMatcherComparisonTest.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return PhenodigmICProfileMatcher.create(kb);
	}

	@Test
	public void testBasic() throws OWLOntologyCreationException, NonUniqueLabelException, FileNotFoundException, UnknownFilterException {
		load();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher1 = PhenodigmICProfileMatcher.create(kb);
		ProfileMatcher profileMatcher2 = GridProfileMatcher.create(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		Double diff = eval.compareMatchers(profileMatcher1, profileMatcher2);
		
		//assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
		
	}

	private void load() throws OWLOntologyCreationException {
		load("/mp-subset.ttl");
		
	}

}
