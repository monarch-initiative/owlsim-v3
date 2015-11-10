package org.monarchinitiative.owlsim.compute.matcher;

import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.cpt.impl.NodeProbabilities;
import org.monarchinitiative.owlsim.compute.matcher.impl.ThreeStateBayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.ThreeStateBayesianNetworkProfileMatcher.BitMapPair;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;

/**
 * Tests 3-state bayesian network matcher, making use of both negative queries and negative
 * associations.
 * 
 * Without negation, a class big-femur may partially match small-femur at the level of 'femur'.
 * With negation, these are disjoint and one should penalize the other 
 * 
 * @author cjm
 *
 */
public class ThreeStateBayesianNetworkProfileMatcherTest extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(ThreeStateBayesianNetworkProfileMatcherTest.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return ThreeStateBayesianNetworkProfileMatcher.create(kb);
	}

	@Test
	public void testBasic() throws Exception {
		loadSimplePhenoWithNegation();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		
		int nOk = 0;
		for (String i : kb.getIndividualIdsInSignature()) {
			
			ProfileQuery pq = profileMatcher.createProfileQuery(i);
			TestQuery tq =  new TestQuery(pq, i, 1); // self should always be ranked first
			String fn = i.replaceAll(".*/", "");
			eval.writeJsonTo("target/bn3-test-results-"+fn+".json");
			Assert.assertTrue(eval.evaluateTestQuery(profileMatcher, tq));

			// try a second time, to test cache
			Assert.assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
			Map<BitMapPair, NodeProbabilities[]> cache = ((ThreeStateBayesianNetworkProfileMatcher) profileMatcher).getTargetToQueryCache();
			LOG.info("CACHE SIZE: "+cache.keySet().size());
			
			if (i.equals("http://x.org/ind-dec-all")) {
				Assert.assertTrue(isRankedLast("http://x.org/ind-inc-all", tq.matchSet));
				nOk++;
			}
			if (i.equals("http://x.org/ind-small-heart-big-brain")) {
				Assert.assertTrue(isRankedLast("http://x.org/ind-big-heart-small-brain", tq.matchSet));
				nOk++;
			}
			if (i.equals("http://x.org/ind-big-femur")) {
				Assert.assertTrue(isRankedLast("http://x.org/ind-small-femur", tq.matchSet));
				nOk++;
			}
			if (i.equals("http://x.org/ind-unstated-phenotype")) {
				//Assert.assertTrue(isRankedLast("http://x.org/ind-no-phenotype", tq.matchSet));
				nOk++;
			}
			if (i.equals("http://x.org/ind-no-brain-phenotype")) {
				Assert.assertTrue(isRankedLast("http://x.org/ind-brain", tq.matchSet));
				nOk++;
			}
			if (i.equals("http://x.org/ind-brain")) {
				Assert.assertTrue(isRankedLast("http://x.org/ind-no-brain-phenotype", tq.matchSet));
				nOk++;
			}
			
		}
		Assert.assertEquals(6, nOk);
	}
	

}
