package org.monarchinitiative.owlsim.compute.matcher;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.BooleanProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;

public class BooleanProfileMatcherTest extends AbstractProfileMatcherTest {

    private Logger LOG = Logger.getLogger(BooleanProfileMatcherTest.class);

    protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
        return BooleanProfileMatcher.create(kb);
    }
    @Test
    public void testBoolean() throws Exception {
        loadSimplePhenoWithNegation();
        //LOG.info("INDS="+kb.getIndividualIdsInSignature());
        ProfileMatcher profileMatcher = createProfileMatcher(kb);

        int nOk = 0;
        for (String i : kb.getIndividualIdsInSignature()) {
            LOG.info("I: "+i);
            if (i.equals("http://x.org/ind-no-brain-phenotype")) {
                continue;
            }
            if (i.equals("http://x.org/ind-unstated-phenotype")) {
                continue;
            }
            ProfileQuery pq = profileMatcher.createProfileQuery(i);
            TestQuery tq =  new TestQuery(pq, i, 1); // self should always be ranked first
            String fn = i.replaceAll(".*/", "");
            eval.writeJsonTo("target/boolean-test-results-"+fn+".json");
            
            LOG.info("Evaluating for "+i);
            eval.evaluateTestQuery(profileMatcher, tq);
            //Assert.assertTrue(eval.evaluateTestQuery(profileMatcher, tq));

            if (i.equals("http://x.org/ind-dec-all")) {
                Assert.assertTrue(isNotInMatchSet("http://x.org/ind-unstated-phenotype", tq.matchSet));
                nOk++;
            }
            if (i.equals("http://x.org/ind-small-heart-big-brain")) {
                Assert.assertTrue(isNotInMatchSet("http://x.org/ind-bone", tq.matchSet));
                nOk++;
            }

        }
        Assert.assertEquals(2, nOk);
    }
    
    @Test
    public void testExampleWithNegation() throws Exception {
        loadSimplePhenoWithNegation();
        //LOG.info("INDS="+kb.getIndividualIdsInSignature());
        ProfileMatcher profileMatcher = createProfileMatcher(kb);

        int nOk = 0;
        String i = "http://x.org/ind-small-heart-big-brain";

        ProfileQuery pq = profileMatcher.createProfileQuery(i);
        TestQuery tq =  new TestQuery(pq, i, 1); // self should always be ranked first
        String fn = i.replaceAll(".*/", "");
        eval.writeJsonTo("target/boolean-extra-test-results-"+fn+".json");
        Assert.assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
        
        Assert.assertTrue(isNotInMatchSet("http://x.org/ind-no-brain-phenotype", tq.matchSet));

    }
}
