package org.monarchinitiative.owlsim.compute.matcher;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.ThreeStateBayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;

/**
 * Tests 3-state bayesian network matcher, making use of both negative queries and negative
 * associations
 * 
 * @author cjm
 *
 */
public class FlatThreeStateBayesianNetworkProfileMatcherTest extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(FlatThreeStateBayesianNetworkProfileMatcherTest.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return ThreeStateBayesianNetworkProfileMatcher.create(kb);
	}

	@Test
	public void testBasic() throws Exception {
		load("no-hierarchy-negation-test.owl");
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		
		int nOk = 0;
		for (String i : kb.getIndividualIdsInSignature()) {
			
			ProfileQuery pq = profileMatcher.createProfileQuery(i);
			TestQuery tq =  new TestQuery(pq, i, 1); // self should always be ranked first
			String fn = i.replaceAll(".*/", "");
			eval.writeJsonTo("target/flat-bn3-test-results-"+fn+".json");
			Assert.assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
			
			if (i.equals("http://x.org/i1")) {
				Assert.assertTrue(isRankedLast("http://x.org/ni1", tq.matchSet));
				nOk++;
			}
			if (i.equals("http://x.org/ni1")) {
				Assert.assertTrue(isRankedLast("http://x.org/i1", tq.matchSet));
				nOk++;
			}
			
		}
		Assert.assertEquals(2, nOk);
	}
	

}
