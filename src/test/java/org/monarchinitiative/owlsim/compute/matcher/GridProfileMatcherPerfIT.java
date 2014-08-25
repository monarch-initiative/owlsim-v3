package org.monarchinitiative.owlsim.compute.matcher;

import org.monarchinitiative.owlsim.compute.matcher.impl.GridProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

public class GridProfileMatcherPerfIT extends AbstractProfileMatcherPerfIT {

	@Override // override in order to simply running as individual unit test
	public void testLarge() throws Exception {
		super.testLarge();
	}
	
	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return GridProfileMatcher.create(kb);
	}


}
