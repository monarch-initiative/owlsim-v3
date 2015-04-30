package org.monarchinitiative.owlsim.compute.matcher.perf;

import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.MaximumInformationContentSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

public class MaximumInformationContentSimilarityProfileMatcherPerfIT extends AbstractProfileMatcherPerfIT {

	public void testLarge() throws Exception {
		super.testLarge();
	}
	
	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return MaximumInformationContentSimilarityProfileMatcher.create(kb);
	}


}
