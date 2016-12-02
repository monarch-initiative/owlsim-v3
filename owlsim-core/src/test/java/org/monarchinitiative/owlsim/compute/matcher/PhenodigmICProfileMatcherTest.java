package org.monarchinitiative.owlsim.compute.matcher;

import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;

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

    @Test
    public void testCompareProfileFile() throws Exception {
        loadSimplePhenoWithNegation();
        //LOG.info("INDS="+kb.getIndividualIdsInSignature());
        ProfileMatcher profileMatcher = createProfileMatcher(kb);

        for (String i : kb.getIndividualIdsInSignature()) {
            Set<String> qcids = kb.getClassIds(kb.getDirectTypesBM(i));
            ProfileQuery qp = profileMatcher.createProfileQueryFromClasses(qcids, null);
            MatchSet matches = profileMatcher.findMatchProfile(qp);
            for (Match match : matches.getMatches()) {
                String j = match.getMatchId();
                Set<String> tcids = kb.getClassIds(kb.getDirectTypesBM(j));
                ProfileQuery tp = profileMatcher.createProfileQueryFromClasses(tcids, null);

                                String fn = i.replaceAll(".*/", "");
                //eval.writeJsonTo("target/pdgm-test-results-"+fn+".json");
                Match pairMatch = profileMatcher.compareProfilePair(qp, tp);
                
                // note: scores may deiverge slightly; this is because
                // disjointness axioms are used for to populate negative class
                // assertions for individuals at KB creation time 
                System.out.println("COMPARING: "+i+" -vs- "+j);
                System.out.println(pairMatch);
                System.out.println(match);
                System.out.println("---");
            }

        }
    }

    public void testBasicWithFilter() throws Exception {
        loadSimplePhenoWithNegation();
        // ProfileQuery pq = profileMatcher.createProfileQuery("http://x.org/ind-dec-all");

    }

}
