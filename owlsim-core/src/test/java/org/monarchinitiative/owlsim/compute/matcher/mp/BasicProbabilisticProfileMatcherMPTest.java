package org.monarchinitiative.owlsim.compute.matcher.mp;

import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightTwoStateProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

public class BasicProbabilisticProfileMatcherMPTest extends AbstractProfileMatcherMPTest {

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb);
	}

	@Override
	public void testSgDiseaseExact() throws Exception {
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
		//testEpDiseaseFuzzy(DISEASE.ep, 100);
	}


	@Override
	public void testNervousSystemDisease() throws Exception {
		//testNervousSystemDisease(DISEASE.ep, 72);
	}
	@Override
	public void testPdDisease() throws Exception {
		testPd(DISEASE.pd, 100);
		
	}

}
