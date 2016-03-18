package org.monarchinitiative.owlsim.eval;

import com.googlecode.javaewah.EWAHCompressedBitmap;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.*;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.junit.Assert;

/**
 * For evaluating test queries against a knowledge base using a profileMatcher
 * 
 * @author cjm
 *
 */
public class ProfileMatchEvaluator {

	private Logger LOG = Logger.getLogger(ProfileMatchEvaluator.class);
	private boolean writeToStdout = false;
	private JSONWriter jsonWriter;

	public void writeJsonTo(String fileName) throws FileNotFoundException {
		jsonWriter = new JSONWriter(fileName);
	}

	public void writeJson(Object obj) {
		jsonWriter.write(obj);
	}

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
	 * @throws UnknownFilterException
	 * @throws IncoherentStateException 
	 */
	public boolean evaluateTestQuery(ProfileMatcher profileMatcher, TestQuery tq) throws OWLOntologyCreationException, NonUniqueLabelException, UnknownFilterException, IncoherentStateException {

		ProfileQuery q = tq.query;
		LOG.info("Q="+q);
		MatchSet mp = profileMatcher.findMatchProfile(q);
		tq.matchSet = mp;
		LOG.info("|Matches|="+mp.getMatches().size());
		if (mp.getMatches().size() == 0) {
			LOG.error("No matches for "+tq+" using "+profileMatcher);
			return false;
		}

		mp.calculateMatchSignificance(mp.getScores());
		LOG.info("first match:"+mp.getMatches().get(0));

		if (jsonWriter != null) {
			LOG.info("Writing MatchSet using "+jsonWriter+" results will appear in "+jsonWriter);
			jsonWriter.write(mp);
		}

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
		LOG.info("Duration "+mp.getExecutionMetadata().getDuration()+" Expected < "+
				tq.maxTimeMs);
		boolean inTime = true;
		if (tq.maxTimeMs != null) {
			if (mp.getExecutionMetadata().getDuration() > tq.maxTimeMs) {
				LOG.error("Execution took too long: " + 
						mp.getExecutionMetadata().getDuration()  + " > " + 
						tq.maxTimeMs);
				inTime = false;
			}
		}
		return actualRank <= tq.maxRank && actualRank > 0 && inTime;

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
		ProfileQuery q;
		if (nqids.size() == 0)
			q = ProfileQueryImpl.create(qids);
		else {
			LOG.info("NQIDS="+nqids);
			q = QueryWithNegationImpl.create(qids, nqids);
		}
		TestQuery tq = new TestQuery(q, expectedId, maxRank);
		return tq;
	}

	/**
	 * Constructs a test query given a set of class labels constituting the query profile
	 * 
	 * if a label follows the pattern "not X" then X is used as the label, and is added
	 * to the set of negated queries - the object returned will be a {@link QueryWithNegation} object.
	 * 
	 * The test query object also holds a pointer to an expected result, and a maximum
	 * ranking at which the match is to be found
	 * 
	 * @param labelMapper
	 * @param expectedMatchLabel
	 * @param maxRank
	 * @param labels - should match the rdfs:label field in the ontology
	 * @return testQuery
	 * @throws NonUniqueLabelException
	 */
	public TestQuery constructTestQuery(
			LabelMapper labelMapper,
			String expectedMatchLabel,
			int maxRank,
			String... labels) throws NonUniqueLabelException {
		Set<String> qids = new HashSet<String>();
		Set<String> nqids = new HashSet<String>();
		for (String label: labels) {
			if (label.startsWith("not ")) {
				nqids.add(labelMapper.lookupByUniqueLabel(label.replaceFirst("not ", "")));
			}
			else {
				String qid = labelMapper.lookupByUniqueLabel(label);
				qids.add(qid);
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

		// expected may be passed in as ID or as label
		Set<String> ids = labelMapper.lookupByLabel(expectedMatchLabel);
		if (ids.size() > 0) {
			expectedMatchLabel = ids.iterator().next();
		}
		TestQuery tq = new TestQuery(q, expectedMatchLabel, maxRank);
		return tq;
	}

	public TestQuery constructTestQueryAgainstIndividual(
			BMKnowledgeBase kb,
			LabelMapper labelMapper,
			String expectedMatchLabel,
			int maxRank,
			String individualLabel) throws NonUniqueLabelException {

		String iid = labelMapper.lookupByUniqueLabel(individualLabel);
		Set<String> qids = kb.getClassIds(kb.getDirectTypesBM(iid));
		LOG.info("QIDS="+qids);
		ProfileQuery q = ProfileQueryImpl.create(qids);

		// expected may be passed in as ID or as label
		Set<String> ids = labelMapper.lookupByLabel(expectedMatchLabel);
		if (ids.size() > 0) {
			expectedMatchLabel = ids.iterator().next();
		}
		TestQuery tq = new TestQuery(q, expectedMatchLabel, maxRank);
		return tq;
	}
	public double compareMatchSetRanks(MatchSet ms1, MatchSet ms2) {
		int totalRankDiff = 0;
		int maxRankDiff = 0;
		String maxRankDiffWitnessId = null;
		int maxRankDiffWitness1 = 0;
		int maxRankDiffWitness2 = 0;
		int n=0;
		for (Match m1 : ms1.getMatches()) {
			Match m2 = ms2.getMatchesWithId(m1.getMatchId());
			int rankDiff = Math.abs(m1.getRank() - m2.getRank());
			totalRankDiff += rankDiff;
			if (rankDiff > maxRankDiff) {
				maxRankDiff = rankDiff;
				maxRankDiffWitnessId = m1.getMatchId();
				maxRankDiffWitness1 = m1.getRank();
				maxRankDiffWitness2 = m2.getRank();			
			}
			n++;
		}
		// TODO: if this isuseful return as an object rather than logging it
		LOG.info("  Max rank diff = "+maxRankDiff+" for match on "+maxRankDiffWitnessId+
				" RANKS= "+maxRankDiffWitness1+","+maxRankDiffWitness2);
		double avgRankDiff = totalRankDiff / (double)n;
		return avgRankDiff;
	}

	public double compareMatchSetP(MatchSet ms1, MatchSet ms2) {
		int totalpdiff = 0;
		int n=0;
		ms1.calculateMatchSignificance(ms1.getScores());
		ms2.calculateMatchSignificance(ms2.getScores());
		for (Match m1 : ms1.getMatches()) {
			Match m2 = ms2.getMatchesWithId(m1.getMatchId());
			totalpdiff += m1.getSignificance() - m2.getSignificance();
			n++;
		}
		double avgPDiff = totalpdiff / (double)n;
		return avgPDiff;
	}

	/**
	 * Compare two profile matchers using all individuals in a KB
	 * 
	 * TODO: replace or extend with Mann-Whitney U test
	 * 
	 * @param pm1
	 * @param pm2
	 * @return average average difference in ranking
	 * @throws IncoherentStateException 
	 * @throws UnknownFilterException 
	 */
	public MatcherComparisonResult compareMatchers(ProfileMatcher pm1, ProfileMatcher pm2) throws UnknownFilterException, IncoherentStateException {
		BMKnowledgeBase kb = pm1.getKnowledgeBase();
		Set<String> inds = kb.getIndividualIdsInSignature();
		double tdiff = 0;
		double maxdiff = 0;
		String maxdiffWitness = null;
		int n = 0;
		LOG.info("Comparing "+pm1+" -vs- "+pm2);
		for (String ind : inds) {
			EWAHCompressedBitmap typesBM = kb.getTypesBM(ind);
			// TODO - add to utils
			//			Set<String> qids = new HashSet<String>();
			//			for (int ix : typesBM.getPositions()) {
			//				qids.add(kb.getClassId(ix));
			//			}
			//			ProfileQuery q = ProfileQueryFactory.createQuery(qids);
			MatchSet ms1 = pm1.findMatchProfile(ind);
			MatchSet ms2 = pm2.findMatchProfile(ind);
			LOG.info("Comparing matchers on "+ind);
			double diff = compareMatchSetRanks(ms1, ms2);
			tdiff += diff;
			if (diff > maxdiff) {
				maxdiff = diff;
				maxdiffWitness = ind;
			}
			n++;
		}
		LOG.info("Total difference = "+tdiff + " / "+n+" runs");
		LOG.info("Max difference = "+maxdiff + " Observed with "+maxdiffWitness);
		double distance = tdiff / (double)n;
		return new MatcherComparisonResult(pm1.getShortName(), pm2.getShortName(), distance);
	}

	public class MatcherComparisonResult {

		public class RankPair {
			public int rank1;
			public int rank2;
			public RankPair(int rank1, int rank2) {
				super();
				this.rank1 = rank1;
				this.rank2 = rank2;
			}


		}

		public String matcher1Type;
		public String matcher2Type;
		public Double distance;
		public Map<String,RankPair> individualToMapPairMap;

		public MatcherComparisonResult(String matcher1Type,
				String matcher2Type, Double distance) {
			super();
			this.matcher1Type = matcher1Type;
			this.matcher2Type = matcher2Type;
			this.distance = distance;

			individualToMapPairMap = new HashMap<String,RankPair>();
		}

		public void addDiff(String ind, int rank1, int rank2) {
			individualToMapPairMap.put(ind, new RankPair(rank1, rank2));
		}


	}

	public List<MatcherComparisonResult> compareAllMatchers(Set<ProfileMatcher> pms) throws UnknownFilterException, IncoherentStateException {
		List<MatcherComparisonResult> results = new ArrayList<MatcherComparisonResult>();
		for (ProfileMatcher pm1 : pms) {
			for (ProfileMatcher pm2 : pms) {
				if (pm1.equals(pm2))
					continue;
				results.add(compareMatchers(pm1, pm2));
			}			
		}
		return results;

	}

	public void runNoiseSimulation(BMKnowledgeBase kb, Set<ProfileMatcher> pms, String dir) throws UnknownFilterException, IncoherentStateException, FileNotFoundException {
		int N = kb.getIndividualIdsInSignature().size();
		EWAHCompressedBitmap tbm = null;
		String tgtId = null;
		while (tgtId == null) {
			int i = (int) (Math.random() * N);
			tgtId = kb.getIndividualId(i);
			tbm = kb.getDirectTypesBM(tgtId);
			if (tbm.cardinality() < 5) {
				tgtId = null; // select again
			}
		}
		ProfileMutator mutator = new ProfileMutator();

		Set<String> tcids = kb.getClassIds(tbm);
		//EWAHCompressedBitmap qbm = tbm.or(tbm); // clone
		ProfileQuery q = ProfileQueryImpl.create(tcids);
		for (int iteration = 0; iteration<10; iteration++) {
			//qbm = addNoise(qbm);
			//q = mutator.addMember(q);
			q = mutator.addBranch(kb, q, 0.1);
			q = mutator.removeBranch(kb, q, 0.3);
			q.setLimit(400);
			for (ProfileMatcher profileMatcher : pms) {
				///ProfileQuery q = createProfileQuery(qbm);
				MatchSet mp = profileMatcher.findMatchProfile(q);
				Match match = mp.getMatchesWithId(tgtId);
				Integer rank;
				Integer erank;
				if (match != null) {
					rank = match.getRank();
					erank = mp.getMatchesWithOrBelowRank(rank).size();
				}
				else {
					rank = null;
					erank = null;
				}
				System.out.println(" "+iteration +
						"\t"+profileMatcher.getShortName()+"\t" + erank+ "\t" + rank+
						"\t"+q.getQueryClassIds().size()+"\t"+tgtId+
						"\t"+rank+"\t"+mp.getExecutionMetadata().getDuration());
				if (dir != null) {
					writeJsonTo(dir+"/"+profileMatcher.getShortName()+"-"+iteration+".json");
					writeJson(mp);
				}
			}
		}

	}

	private EWAHCompressedBitmap addNoise(EWAHCompressedBitmap qbm) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProfileQuery createProfileQuery(EWAHCompressedBitmap qbm) {
		// TODO Auto-generated method stub
		return null;
	}
}
