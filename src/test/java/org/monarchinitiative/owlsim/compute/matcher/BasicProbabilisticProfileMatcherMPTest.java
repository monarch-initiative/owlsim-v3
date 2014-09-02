package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;

import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherMPTest.DISEASE;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaivesBayesFixedWeightProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class BasicProbabilisticProfileMatcherMPTest extends AbstractProfileMatcherMPTest {

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return NaivesBayesFixedWeightProfileMatcher.create(kb);
	}

	@Override
	public void testSgDiseaseExact() throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		testSgDiseaseExact(DISEASE.sg, 100);

	}

	@Override
	public void testSgDiseaseLeaveOneOut() throws Exception {
		testSgDiseaseLeaveOneOut(DISEASE.sg, 100);
	}


	@Override
	public void testMultiPhenotypeDisease() throws Exception {
		// we expect Foo and pd to rank the same
		testMultiPhenotypeDisease(null, 100);
	}


	@Override
	public void testEpDiseaseFuzzy() throws Exception {
		testEpDiseaseFuzzy(DISEASE.ep, 100);
	}


	@Override
	public void testNervousSystemDisease() throws Exception {
		testNervousSystemDisease(DISEASE.ep, 100);
	}
	@Override
	public void testPdDisease() throws Exception {
		testNervousSystemDisease(DISEASE.pd, 100);
		
	}

}
