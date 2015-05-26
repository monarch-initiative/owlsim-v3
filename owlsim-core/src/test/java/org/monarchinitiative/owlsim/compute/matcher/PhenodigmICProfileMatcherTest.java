package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests bayesian network matcher, making use of both negative queries and negative
 * associations
 * 
 * @author cjm
 *
 */
public class PhenodigmICProfileMatcherTest extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(PhenodigmICProfileMatcherTest.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return PhenodigmICProfileMatcher.create(kb);
	}

	@Test
	public void testBasic() throws Exception {
		loadSimplePhenoWithNegation();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		
		int nOk = 0;
		for (String i : kb.getIndividualIdsInSignature()) {
			if (i.equals("http://x.org/ind-no-brain-phenotype")) {
				continue;
			}
			if (i.equals("http://x.org/ind-unstated-phenotype")) {
				continue;
			}
			ProfileQuery pq = profileMatcher.createProfileQuery(i);
			TestQuery tq =  new TestQuery(pq, i, 1); // self should always be ranked first
			String fn = i.replaceAll(".*/", "");
			eval.writeJsonTo("target/pdgm-test-results-"+fn+".json");
			Assert.assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
			
			if (i.equals("http://x.org/ind-dec-all")) {
				Assert.assertTrue(isRankedLast("http://x.org/ind-unstated-phenotype", tq.matchSet));
				nOk++;
			}
			if (i.equals("http://x.org/ind-small-heart-big-brain")) {
				Assert.assertTrue(isRankedLast("http://x.org/ind-bone", tq.matchSet));
				nOk++;
			}
			
		}
		Assert.assertEquals(2, nOk);
	}
	

}
