package org.monarchinitiative.owlsim.compute.matcher.mp;

import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.JaccardSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

public class JaccardSimilarityProfileMatcherMPTest extends AbstractProfileMatcherMPTest {

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return JaccardSimilarityProfileMatcher.create(kb);
	}

	@Override
	public void testSgDiseaseExact() throws Exception {
		testSgDiseaseExact(DISEASE.sg, null);

	}

	@Override
	public void testSgDiseaseLeaveOneOut() throws Exception {
		testSgDiseaseLeaveOneOut(DISEASE.sg, null);
	}


	@Override
	public void testMultiPhenotypeDisease() throws Exception {
		testMultiPhenotypeDisease(DISEASE.foo, null);
	}


	@Override
	public void testEpDiseaseFuzzy() throws Exception {
		testEpDiseaseFuzzy(DISEASE.ep, null);
	}


	@Override
	public void testNervousSystemDisease() throws Exception {
		testNervousSystemDisease(DISEASE.ep, null);
	}
	@Override
	public void testPdDisease() throws Exception {
		//testNervousSystemDisease(DISEASE.pd, 100);
		
	}
}
