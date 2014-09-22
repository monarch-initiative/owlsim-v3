package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.GMProfileMatcher;
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
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.Sets;

/**
 * Tests a ProfileMatcher using the sample species ontology
 * 
 * 
 * @author cjm
 *
 */
public class GMProfileMatcherSpeciesTest {

	protected BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(GMProfileMatcherSpeciesTest.class);
	protected boolean writeToStdout = true;
	List<TestQuery> testQueries = new ArrayList<TestQuery>();
	
	private class TestQuery {
		ProfileQuery query;
		String expectedId;
		int maxRank = 1;
		public TestQuery(ProfileQuery query, String expectedId) {
			super();
			this.query = query;
			this.expectedId = expectedId;
		}
		public TestQuery(ProfileQuery query, String expectedId, int maxRank) {
			super();
			this.query = query;
			this.expectedId = expectedId;
			this.maxRank = maxRank;
		}
		
		
		
	}

	private void addQuery(ProfileQuery q, String expectedId, int maxRank) {
		testQueries.add(new TestQuery(q, getId(expectedId), maxRank));
	}
	private void addQuery(ProfileQuery q, String expectedId) {
		addQuery(q, expectedId, 1);
	}
	

	@Test
	public void testGM() throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		load("species.owl");
		setQueries();
		LOG.info("CLASSES: "+kb.getClassIdsInSignature());
		testMatcher(GMProfileMatcher.create(kb));
	}
	

	private void setQueries() throws NonUniqueLabelException {
		
//		addQuery(getQuery("spider"), "ProtoSpider", 1);
		addQuery(getQuery("shark", "octopus"), "Sharktopus", 1);
		
		addQuery(getQuery("poriferan", "human"), "SpongeBob");
		addQuery(getQuery("arthropod", "human"), "SpiderMan"); // more general
		addQuery(getQuery("fruitfly", "human"), "SpiderMan", 2); // sib. 
		addQuery(getQuery("squid", "shark"), "Sharktopus", 3); // sib

		addQuery(getQuery("tarantula", "human"), "SpiderMan", 1); // more specific
		addQuery(getQuery("spider", "mouse"), "SpiderMan", 3); // proto-mammal and proto-spider are better

		// cephalopod-human hybrids
		addQuery(getQuery("xenopus", "human", "cuttlefish"), "GreatOldOne", 1); // MaxIC ranks smallTrait as best
		addQuery(getQuery("amphibian", "human", "cuttlefish"), "GreatOldOne", 1);
		addQuery(getQuery("xenopus", "human"), "GreatOldOne", 2); // leave one out
		addQuery(getQuery("octopus", "human"), "GreatOldOne", 1);
		addQuery(getQuery("octopus", "human"), "SquidMan", 4);
//	
		addQuery(getQuery("insect", "human"), "SpiderMan", 3);
		addQuery(getQuery("fruitfly", "rat"), "SmallTrait", 1);  // expected, as human close to mouse

		addQuery(getQuery("shark"), "ProtoShark", 1);
		addQuery(getQuery("dolphin"), "ProtoMammal", 1);
		addQuery(getQuery("dolphin"), "ProtoWhale", 2);
		addQuery(getQuery("cat", "dog", "mouse", "human"), "DogMouse", 2);
		addQuery(getQuery("cat", "dog", "mouse", "human"), "SuperMammal", 3);
		addQuery(getQuery("cat", "dog", "mouse", "human"), "ProtoMammal", 1);
//		
//		// we get a low rank here as 'swimming trait' is specified using generic taxa
		addQuery(getQuery("dolphin", "blueWhale", "zebrafish"), "SwimmingTrait", 1);
		addQuery(getQuery("cetacean", "shark"), "BigTrait", 1);
//		
//		// note that it's necessary to 'hold all the cards' to maximize cute trait
//		// for scores that test all features of matched entity, e.g. SimJ
		addQuery(getQuery("koala"), "CuteTrait", 10);
		
		
	}
	
	private String getId(String label) {
		return "http://x.org/"+label;
	}
	
	private ProfileQuery getQuery(String... labels) throws NonUniqueLabelException {
		Set<String> qids = new HashSet<String>();
		for (String label: labels) {
			qids.add(getId(label));
		}
		LOG.info("QIDS="+qids);
		return ProfileQueryImpl.create(qids);
	}
	
	private void testMatcher(ProfileMatcher profileMatcher) throws OWLOntologyCreationException, NonUniqueLabelException, FileNotFoundException, UnknownFilterException {

		for (TestQuery tq : testQueries) {
			ProfileQuery q = tq.query;
			LOG.info("Q="+q);
			MatchSet mp = profileMatcher.findMatchProfile(q);
			
			JSONWriter w = new JSONWriter("target/species-match-results.json");
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
		


	}

	private void load(String fn) throws OWLOntologyCreationException {
		OWLLoader loader = new OWLLoader();
		loader.load("src/test/resources/"+fn);
		kb = loader.createKnowledgeBaseInterface();
	}


}
