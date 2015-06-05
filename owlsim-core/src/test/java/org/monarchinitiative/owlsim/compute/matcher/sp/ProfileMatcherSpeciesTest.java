package org.monarchinitiative.owlsim.compute.matcher.sp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.GridProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.JaccardSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests a ProfileMatcher using the sample species ontology
 * 
 * 
 * @author cjm
 *
 */
public class ProfileMatcherSpeciesTest {

	protected BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(ProfileMatcherSpeciesTest.class);
	protected boolean writeToStdout = true;
	List<TestQuery> testQueries = new ArrayList<TestQuery>();
	

	private void addQuery(ProfileQuery q, String expectedId, int maxRank) {
		testQueries.add(new TestQuery(q, getId(expectedId), maxRank));
	}
	private void addQuery(ProfileQuery q, String expectedId) {
		addQuery(q, expectedId, 1);
	}
	
	@Test
	public void test1() throws Exception {
		load("species.owl");
		setQueries();
		LOG.info("CLASSES: "+kb.getClassIdsInSignature());
		testMatcher(GridProfileMatcher.create(kb));
		testMatcher(JaccardSimilarityProfileMatcher.create(kb));
		//testMatcher(MaximumInformationContentSimilarityProfileMatcher.create(kb));
		//testMatcher(NaiveBayesFixedWeightProfileMatcher.create(kb));
	}

	@Test
	public void testBN() throws Exception {
		load("species.owl");
		setQueries();
		LOG.info("CLASSES: "+kb.getClassIdsInSignature());
		//testMatcher(BayesianNetworkProfileMatcher.create(kb));
	}

	@Test
	public void testGM() throws Exception {
		load("species.owl");
		setQueries();
		LOG.info("CLASSES: "+kb.getClassIdsInSignature());
		//testMatcher(GMProfileMatcher.create(kb));
	}
	

	private void setQueries() throws NonUniqueLabelException {
		
		addQuery(getQuery("spider"), "ProtoSpider");
		addQuery(getQuery("shark", "octopus"), "Sharktopus");
		addQuery(getQuery("poriferan", "human"), "SpongeBob");
		addQuery(getQuery("arthropod", "human"), "SpiderMan"); // more general
		addQuery(getQuery("tarantula", "human"), "SpiderMan"); // more specific
		addQuery(getQuery("spider", "mouse"), "SpiderMan", 2);

		addQuery(getQuery("insect", "human"), "SpiderMan", 2);

		// cephalopod-human hybrids
		addQuery(getQuery("xenopus", "human", "cuttlefish"), "GreatOldOne", 2); // MaxIC ranks smallTrait as best
		addQuery(getQuery("amphibian", "human", "cuttlefish"), "GreatOldOne", 1);
		addQuery(getQuery("xenopus", "human"), "GreatOldOne", 2);
		addQuery(getQuery("octopus", "human"), "GreatOldOne", 3);
		addQuery(getQuery("octopus", "human"), "SquidMan", 2);
	
		// the query insect+human intuitively seems close to SpiderMan,
		// but it may also score close to other items
		addQuery(getQuery("insect", "human"), "SpiderMan", 3);
		//addQuery(getQuery("insect", "human"), "PorpoiseMarmosetFly", 3);
		//addQuery(getQuery("insect", "human"), "SmallTrait", 3);  // expected, as human close to mouse

		addQuery(getQuery("shark"), "ProtoShark", 1);
		addQuery(getQuery("dolphin"), "ProtoCetacean", 1);
		addQuery(getQuery("cat", "dog", "mouse", "human"), "DogMouse", 2);
		addQuery(getQuery("cat", "dog", "mouse", "human"), "SuperMammal", 3);
		
		// we get a low rank here as 'swimming trait' is specified using generic taxa
		addQuery(getQuery("dolphin", "blueWhale", "zebrafish"), "SwimmingTrait", 5);
		addQuery(getQuery("cetacean", "shark"), "BigTrait", 3);
		
		// note that it's necessary to 'hold all the cards' to maximize cute trait
		// for scores that test all features of matched entity, e.g. SimJ
		addQuery(getQuery("koala"), "CuteTrait", 3);
		
		
		
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
	
	private void testMatcher(ProfileMatcher profileMatcher) throws Exception {

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
			LOG.info("Rank of "+tq.expectedId+" == "+actualRank+" when using "+profileMatcher+" expected to be <="+tq.maxRank);
			
			Assert.assertTrue(actualRank <= tq.maxRank);
		}
		


	}

	private void load(String fn) throws OWLOntologyCreationException {
		OWLLoader loader = new OWLLoader();
		loader.load("src/test/resources/"+fn);
		kb = loader.createKnowledgeBaseInterface();
	}


}
