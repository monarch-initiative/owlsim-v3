package org.monarchinitiative.owlsim.eval;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.BasicQuery;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.impl.BasicQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * For evaluating test queries against a knowledge base using a profileMatcher
 * 
 * @author cjm
 *
 */
public class Evaluator {
	
	private Logger LOG = Logger.getLogger(Evaluator.class);
	private boolean writeToStdout = true;

	/**
	 * given a test query (a query plus expected results) and a matcher,
	 * run the matcher with the query and evaluate results
	 *
	 * @param profileMatcher
	 * @param tq
	 * @return true if succeeds
	 * @throws OWLOntologyCreationException
	 * @throws NonUniqueLabelException
	 * @throws FileNotFoundException
	 * @throws UnknownFilterException
	 */
	public boolean evaluateTestQuery(ProfileMatcher profileMatcher, TestQuery tq) throws OWLOntologyCreationException, NonUniqueLabelException, FileNotFoundException, UnknownFilterException {

		BasicQuery q = tq.query;
		LOG.info("Q="+q);
		MatchSet mp = profileMatcher.findMatchProfile(q);

		JSONWriter w = new JSONWriter("target/gm-match-results.json");
		w.write(mp);

		if (writeToStdout) {
			//Gson gson = new GsonBuilder().setPrettyPrinting().create();
			//String json = gson.toJson(mp);
			System.out.println(mp);
		}
		int actualRank = -1;
		for (Match m : mp.getMatches()) {
			if (m.getMatchId().equals(tq.expectedId)) {
				actualRank = m.getRank();
			}
		}
		LOG.info("Rank of "+tq.expectedId+" == "+actualRank+" when using "+profileMatcher);

		return actualRank <= tq.maxRank;

	}

	/**
	 * @param expectedId
	 * @param maxRank
	 * @param qidArr
	 * @return testQuery
	 */
	public TestQuery constructTestQuery(String expectedId,
			int maxRank,
			String... qidArr) {
		Set<String> qids = new HashSet<String>();
		for (String qid: qidArr) {
			qids.add(qid);
		}
		BasicQuery q = BasicQueryImpl.create(qids);
		TestQuery tq = new TestQuery(q, expectedId, maxRank);
		return tq;
	}
	
	/**
	 * @param labelMapper
	 * @param expectedId
	 * @param maxRank
	 * @param labels
	 * @return testQuery
	 * @throws NonUniqueLabelException
	 */
	public TestQuery constructTestQuery(
			LabelMapper labelMapper,
			String expectedId,
			int maxRank,
			String... labels) throws NonUniqueLabelException {
		Set<String> qids = new HashSet<String>();
		Set<String> nqids = new HashSet<String>();
		for (String label: labels) {
			if (label.startsWith("not ")) {
				nqids.add(labelMapper.lookupByUniqueLabel(label.replaceFirst("not ", "")));
			}
			else {
				qids.add(labelMapper.lookupByUniqueLabel(label));
			}
		}
		LOG.info("QIDS="+qids);
		BasicQuery q;
		if (nqids.size() == 0)
			q = BasicQueryImpl.create(qids);
		else {
			LOG.info("NQIDS="+nqids);
			q = QueryWithNegationImpl.create(qids, nqids);
		}
		
		// expected may be passed in as ID or as label
		Set<String> ids = labelMapper.lookupByLabel(expectedId);
		if (ids.size() > 0) {
			expectedId = ids.iterator().next();
		}
		TestQuery tq = new TestQuery(q, expectedId, maxRank);
		return tq;
	}


}
