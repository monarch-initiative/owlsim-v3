package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.monarchinitiative.owlsim.eval.ProfileMatchEvaluator;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * 
 * Assumptions:
 * - all URIs are http://x.org/ (may change with CURIEs)
 * 
 * @author cjm
 *
 */
public class AbstractProfileMatcherTest {

	protected BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(AbstractProfileMatcherTest.class);
	protected boolean writeToStdout = false;
	protected ProfileMatchEvaluator eval = new ProfileMatchEvaluator();


	protected boolean isUseLabels() {
		return false;
	}
	
	/**
	 * Fetch an ID by a name; is isUseLabels is set, then this will
	 * do a label lookup; otherwise affix name onto standard prefix
	 * 
	 * @param label
	 * @return
	 */
	protected String getId(String label) {
		if (isUseLabels()) {
			try {
				return kb.getLabelMapper().lookupByUniqueLabel(label);
			} catch (NonUniqueLabelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "http://x.org/"+label;
	}

	/**
	 * @param q
	 * @param expectedId
	 * @param maxRank
	 * @return test query
	 */
	protected TestQuery getTestQuery(ProfileQuery q, String expectedId, int maxRank) {
		return new TestQuery(q, getId(expectedId), maxRank);
	}

	/**
	 * @param q
	 * @param expectedId
	 */
	protected void getTestQuery(ProfileQuery q, String expectedId) {
		getTestQuery(q, expectedId, 1);
	}

	/**
	 * Load an ontology from resources folder
	 * 
	 * @param fn
	 * @throws OWLOntologyCreationException
	 */
	protected void load(String fn) throws OWLOntologyCreationException {
		OWLLoader loader = new OWLLoader();
		loader.load("src/test/resources/"+fn);
		kb = loader.createKnowledgeBaseInterface();
	}

	/**
	 * Load ontology plus data ontologies
	 * 
	 * @param fn
	 * @param ontfns
	 * @throws OWLOntologyCreationException
	 */
	protected void load(String fn, String... ontfns) throws OWLOntologyCreationException {
		OWLLoader loader = new OWLLoader();
		LOG.info("R="+fn);
		loader.load(getClass().getResource(fn).getFile());
		for (String ontfn : ontfns) {
			LOG.info("R="+ontfn);
			URL res = getClass().getResource(ontfn);
			LOG.info("RES="+res);
			loader.loadOntologies(res.getFile());
		}
		kb = loader.createKnowledgeBaseInterface();
	}
	
    protected void loadSimplePhenoWithNegation() throws OWLOntologyCreationException {
        load("simple-pheno-with-negation.owl"); 
    }

    protected void loadSimplePhenoWithFrequency() throws OWLOntologyCreationException {
        load("simple-pheno-with-freqs.owl"); 
    }

	@Deprecated
	protected void search(ProfileMatcher profileMatcher,
			String expectedId, int maxRank,
			String... labels) throws UnknownFilterException, Exception {
		TestQuery tq = constructTestQuery(expectedId, maxRank, labels);
		evaluateTestQuery(profileMatcher, tq);
	}	

	@Deprecated
	protected TestQuery constructTestQuery(String expectedId, int maxRank,
			String... labels) {
		Set<String> qids = new HashSet<String>();
		Set<String> nqids = new HashSet<String>();
		for (String label: labels) {
			if (label.startsWith("not ")) {
				nqids.add(getId(label.replaceFirst("not ", "")));
			}
			else {
				qids.add(getId(label));
			}
		}
		LOG.info("QIDS="+qids);
		ProfileQuery q;
		if (nqids.size() == 0)
			q = ProfileQueryImpl.create(qids);
		else {
			LOG.info("NQIDS="+nqids);
			q = QueryWithNegationImpl.create(qids, nqids);
		}
		TestQuery tq = new TestQuery(q, getId(expectedId), maxRank);
		return tq;
	}
	

	/**
	 * given a test query (a query plus expected results) and a matcher,
	 * run the matcher with the query and evaluate results
	 * 
	 * @param profileMatcher
	 * @param tq
	 * @throws OWLOntologyCreationException
	 * @throws NonUniqueLabelException
	 * @throws FileNotFoundException
	 * @throws UnknownFilterException
	 */
	protected void evaluateTestQuery(ProfileMatcher profileMatcher, TestQuery tq) throws Exception {

		Assert.assertTrue(eval.evaluateTestQuery(profileMatcher, tq));


	}
	
	protected boolean isRankedLast(String matchId, MatchSet matchSet) {
		int matchRank = 0;
		for (Match m : matchSet.getMatches()) {
			int rank = m.getRank();
			
			if (m.getMatchId().equals(matchId)) {
				matchRank = rank;
			}
			else {
				if (matchRank > 0 && rank > matchRank) {
					LOG.error("Ranking of match "+matchId+" is "+matchRank+" which is < "+m);
					return false;
				}
			}
		}
		if (matchRank == 0) {
			LOG.error("Not found: "+matchId);
			return false;
		}
		LOG.info("Rank of match "+matchId+" is "+matchRank+" which is last or joint last");
		return true;
	}
	
	protected boolean isNotInMatchSet(String matchId, MatchSet matchSet) {
	    for (Match m : matchSet.getMatches()) {
	        if (m.getMatchId().equals(matchId)) {
	           return false;
	        }
	    }
	    return true;
	}


	
	protected boolean isRankedAt(String matchId, MatchSet matchSet, int expectedRank) {
	    int matchRank = 0;
	    for (Match m : matchSet.getMatches()) {
	        int rank = m.getRank();

	        if (m.getMatchId().equals(matchId)) {
	            return (rank == expectedRank);
	        }
	    }
	    return false;
	}

}
