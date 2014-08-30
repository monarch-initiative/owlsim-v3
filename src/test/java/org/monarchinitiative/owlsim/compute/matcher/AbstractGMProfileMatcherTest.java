package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.BasicProbabilisticProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.GMProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.GMProfileMatcher.GMProfileMatcherConfig;
import org.monarchinitiative.owlsim.compute.matcher.impl.GridProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.JaccardSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.MaximumInformationContentSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.BasicQuery;
import org.monarchinitiative.owlsim.model.match.impl.BasicQueryImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;

/**
 * Tests a ProfileMatcher using ultra-simple ontoligy
 * 
 *     Thing
 *     /    \
 *    a      b
 *    
 * 
 * With 3 'genes', with annotations {a}, {b}, and {a,b}
 * 
 *
 */
public class AbstractGMProfileMatcherTest {

	protected BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(AbstractGMProfileMatcherTest.class);
	protected boolean writeToStdout = true;

	private class TestQuery {
		BasicQuery query;
		String expectedId;
		int maxRank = 1;
		public TestQuery(BasicQuery query, String expectedId) {
			super();
			this.query = query;
			this.expectedId = expectedId;
		}
		public TestQuery(BasicQuery query, String expectedId, int maxRank) {
			super();
			this.query = query;
			this.expectedId = expectedId;
			this.maxRank = maxRank;
		}



	}

	private String getId(String label) {
		return "http://x.org/"+label;
	}

	private TestQuery getTestQuery(BasicQuery q, String expectedId, int maxRank) {
		return new TestQuery(q, getId(expectedId), maxRank);
	}

	private void getTestQuery(BasicQuery q, String expectedId) {
		getTestQuery(q, expectedId, 1);
	}

	protected void search(String expectedId, int maxRank,
			double resampleProbability,
			String... labels) throws NonUniqueLabelException, OWLOntologyCreationException, FileNotFoundException, UnknownFilterException {
		GMProfileMatcherConfig config = new GMProfileMatcherConfig();
		config.q = resampleProbability;
		GMProfileMatcher profileMatcher = (GMProfileMatcher) GMProfileMatcher.create(kb, config);
		Set<String> qids = new HashSet<String>();
		for (String label: labels) {
			qids.add(getId(label));
		}
		LOG.info("QIDS="+qids);
		LOG.info("Resample proabability q="+profileMatcher.getResampleProbability());
		BasicQuery q = BasicQueryImpl.create(qids);
		TestQuery tq = new TestQuery(q, getId(expectedId), maxRank);
		testMatcher(profileMatcher, tq);
	}	


	protected void testMatcher(ProfileMatcher profileMatcher, TestQuery tq) throws OWLOntologyCreationException, NonUniqueLabelException, FileNotFoundException, UnknownFilterException {

		BasicQuery q = tq.query;
		LOG.info("Q="+q);
		LOG.info("Resample proabability q="+((GMProfileMatcher) profileMatcher).getResampleProbability());
		MatchSet mp = profileMatcher.findMatchProfile(q);

		JSONWriter w = new JSONWriter("target/gm-match-results.json");
		w.write(mp);

		if (writeToStdout) {
			//Gson gson = new GsonBuilder().setPrettyPrinting().create();
			//String json = gson.toJson(mp);
			System.out.println(mp);
		}
		List<Match> topMatches = mp.getMatchesWithRank(1);
		int actualRank = -1;
		for (Match m : mp.getMatches()) {
			if (m.getMatchId().equals(tq.expectedId)) {
				actualRank = m.getRank();
			}
		}
		LOG.info("Rank of "+tq.expectedId+" == "+actualRank+" when using "+profileMatcher);

		Assert.assertTrue(actualRank <= tq.maxRank);



	}

	protected void load(String fn) throws OWLOntologyCreationException {
		OWLLoader loader = new OWLLoader();
		loader.load("src/test/resources/"+fn);
		kb = loader.createKnowledgeBaseInterface();
	}


}
