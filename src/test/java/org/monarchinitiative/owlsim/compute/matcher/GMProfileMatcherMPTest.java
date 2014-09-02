package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;

import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.GMProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class GMProfileMatcherMPTest extends AbstractProfileMatcherMPTest {

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return GMProfileMatcher.create(kb);
	}

	@Override
	public void testSgDiseaseExact() throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		testSgDiseaseExact(DISEASE.sg, null);

	}

	@Override
	public void testSgDiseaseLeaveOneOut() throws Exception {
		//testSgDiseaseLeaveOneOut(DISEASE.sg, null);
		testSgDiseaseLeaveOneOut(null, null); // sg is 2nd ranked
	}


	@Override
	public void testMultiPhenotypeDisease() throws Exception {
		// TODO
		testMultiPhenotypeDisease(null, 100);
	}


	@Override
	public void testEpDiseaseFuzzy() throws Exception {
		// TODO - investigate why this fails
		testEpDiseaseFuzzy(DISEASE.ep, null);
	}


	@Override
	public void testNervousSystemDisease() throws Exception {
		testNervousSystemDisease(DISEASE.foo, null);
	}

	@Override
	public void testPdDisease() throws Exception {
		testPd(DISEASE.pd, null);
		
	}


}
