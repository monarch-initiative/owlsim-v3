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
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.BasicQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * For evaluating test queries against a knowledge base using a profileMatcher
 * 
 * @author cjm
 *
 */
public class ProfileMatchEvaluator {
	
	private Logger LOG = Logger.getLogger(ProfileMatchEvaluator.class);
	private boolean writeToStdout = true;

	/**
	 * given a test query (a query plus expected results) and a matcher,
	 * run the matcher with the query and evaluate results.
	 * 
	 * Use {@link #constructTestQuery(LabelMapper, String, int, String...)} to construct a TestQuery object
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
	 * Constructs a test query using a set of URIs as input.
	 * 
	 * if a URI follows the pattern "not X" then X is used as the label, and is added
	 * to the set of negated queries - the object returned will be a {@link QueryWithNegation} object.
	 * 
	 * @param expectedId
	 * @param maxRank
	 * @param qidArr
	 * @return testQuery
	 */
	public TestQuery constructTestQuery(String expectedId,
			int maxRank,
			String... qidArr) {
		Set<String> qids = new HashSet<String>();
		Set<String> nqids = new HashSet<String>();
		for (String qid: qidArr) {
			if (qid.startsWith("not ")) {
				nqids.add(qid.replaceFirst("not ", ""));
			}
			else {
				qids.add(qid);
			}
		}
		BasicQuery q;
		if (nqids.size() == 0)
			q = BasicQueryImpl.create(qids);
		else {
			LOG.info("NQIDS="+nqids);
			q = QueryWithNegationImpl.create(qids, nqids);
		}
		TestQuery tq = new TestQuery(q, expectedId, maxRank);
		return tq;
	}
	
	/**
	 * Constructs a test query using labels as inputs.
	 * 
	 * if a label follows the pattern "not X" then X is used as the label, and is added
	 * to the set of negated queries - the object returned will be a {@link QueryWithNegation} object.
	 * 
	 * 
	 * @param labelMapper
	 * @param expectedId
	 * @param maxRank
	 * @param labels - should match the rdfs:label field in the ontology
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
