package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
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
 * Tests a ProfileMatcher using the sample mp-subset.ttl ontology
 * 
 * This ontology has the following disease-phenotype associations:
 * 
 * <sg> rdfs:label "Small gonads disease (fake for testing)" .
 *   ## 'absent gametes'
 *   ## 'small gonad'
 * <ep> rdfs:label "Epilepsy (fake for testing)" .
 *   ## 'abnormal CNS synaptic transmission'
 *   ## seizures
 * <foo>
 *   ## 'abnormal cerebellum external granule cell layer morphology'
 * <pd>
 *   ## 'abnormal basal ganglion morphology'
 *   ## 'abnormal cerebellum external granule cell layer morphology'
 *   ## 'abnormal long term depression'
 *   ## 'impaired coordination'
 *   
 * This test is subclasses by specific tests for different profileMatchers,
 * but we may refactor this in future
 *   
 * @author cjm
 *
 */
public abstract class AbstractProfileMatcherMPTest {

	protected BMKnowledgeBase ontology;
	protected ProfileMatcher profileMatcher;
	private Logger LOG = Logger.getLogger(AbstractProfileMatcherMPTest.class);
	protected boolean writeToStdout = true;
	
	protected enum DISEASE {
		sg,
		foo,
		ep,
		pd
	};

	/**
	 * Performs a test using a query that is identical to the annotation
	 * profile for 'small gonads disease' (sg):
	 * 		
	 * ## 'absent gametes'
	 * ## 'small gonad'
	 * 
	 * In all cases, we would expect sg to rank as top
	 *
	 * @throws Exception
	 */
	@Test
	public abstract void testSgDiseaseExact() throws Exception;

	protected void testSgDiseaseExact(DISEASE d, Number expectedScore) throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		testQuery(Sets.newHashSet(
				"absent gametes", 
				"small gonad"
				),
				d,
				expectedScore);
	}


	/**
	 * Performs a query using a single class in the query, which is
	 * identical to one of the 2 classes used to annotate sg.
	 * 
	 * because this class is not used elsewhere, in general we
	 * expect sg to still be top 
	 * 
	 * ## 'small gonad'
	 * @throws Exception 
	 */
	@Test
	public abstract void testSgDiseaseLeaveOneOut() throws Exception;
	protected void testSgDiseaseLeaveOneOut(DISEASE d, Number expectedScore) throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		testQuery(Sets.newHashSet(
				"small gonad"
				),
				d,
				expectedScore);
	}

	/**
	 * This test uses a profile of 3 classes which span different diseases,
	 * including pd, ep, foo and ep
	 * 
	 * @throws Exception
	 */
	@Test
	public abstract void testMultiPhenotypeDisease() throws Exception;
	protected void testMultiPhenotypeDisease(DISEASE d, Number expectedScore) throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		testQuery(Sets.newHashSet(
				"reproductive system phenotype",   // present in sg
				"abnormal cerebellum development",  // superclass of phenotype in pd and foo
				"abnormal synaptic transmission"    // in ep
				),
				d, 
				expectedScore);
	}

	/**
	 * Searches using a profile that is a more generic version of the
	 * phenotype profile for ep
	 * 
	 * ep has:
	 * ## 'abnormal CNS synaptic transmission'
	 * ## seizures
	 * 
	 * the query is:
	 * "reproductive system phenotype",  // not in disease profile, weakly matches sg
	 * "abnormal synaptic transmission"  // more general that the phenotype in ep
	 * 
	 * @throws Exception 
	 */
	@Test
	public abstract void testEpDiseaseFuzzy() throws Exception;
	protected void testEpDiseaseFuzzy(DISEASE d, Number expectedScore) throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		testQuery(Sets.newHashSet(
				"reproductive system phenotype",  // not in disease profile
				"abnormal behavior",
				"abnormal nervous system physiology",
				"abnormal synaptic transmission"  // more general
				),
				d,
				expectedScore);
	}

	/**
	 * A highly generic query expected to match foo, pd and ep
	 * 
	 * @throws Exception
	 */
	@Test
	public abstract void testNervousSystemDisease() throws Exception;
	protected void testNervousSystemDisease(DISEASE d, Number expectedScore) throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		testQuery(Sets.newHashSet(
				"nervous system phenotype"  // foo, pd and ep all have this
				),
				d,
				expectedScore);
	}

	/**
	 * should be precise match for pd
	 *   ## 'abnormal basal ganglion morphology'
	 *   ## 'abnormal cerebellum external granule cell layer morphology'
	 *   ## 'abnormal long term depression'
	 *   ## 'impaired coordination'
	 * 
	 * @throws Exception
	 */
	@Test
	public abstract void testPdDisease() throws Exception;
	protected void testPd(DISEASE d, Number expectedScore) throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		testQuery(Sets.newHashSet(
				"abnormal basal ganglion morphology", 
				"abnormal cerebellum external granule cell layer morphology",
				"abnormal long term depression",
				"impaired coordination" 
				),
				d,
				expectedScore);
	}

	private void testQuery(Set<String> queryClassLabels, 
			DISEASE expectedDisease, 
			Number expectedScore) throws OWLOntologyCreationException, NonUniqueLabelException, FileNotFoundException, UnknownFilterException {

		String expectedDiseaseFrag = expectedDisease == null ? null : expectedDisease.toString();
		load("mp-subset.ttl");
		LOG.info("Ontology = "+profileMatcher.getKnowledgeBase());
		ProfileQuery q = ProfileQueryImpl.create(queryClassLabels, profileMatcher.getKnowledgeBase().getLabelMapper());
		LOG.info("Query = "+q);
		MatchSet mp = profileMatcher.findMatchProfile(q);

		JSONWriter w = new JSONWriter("target/match-results.json");
		w.write(mp);

		if (writeToStdout) {
			//Gson gson = new GsonBuilder().setPrettyPrinting().create();
			//String json = gson.toJson(mp);
			System.out.println(mp);
		}
		List<Match> topMatches = mp.getMatchesWithRank(1);
		LOG.info("topMatches="+topMatches+" //Expected="+expectedDiseaseFrag+" IN "+profileMatcher);
		if (expectedDiseaseFrag != null) {
			boolean isMatchesExpected = false;
			for (Match m : topMatches) {
				if (m.getMatchId().contains("/"+expectedDiseaseFrag)) {
					isMatchesExpected = true;

					if (expectedScore != null) {
						if (expectedScore instanceof Integer) {
							Assert.assertTrue(m.getPercentageScore() == expectedScore.intValue());
						}
						else {
							LOG.warn("FOO");
						}
					}

				}
			}
			Assert.assertTrue(isMatchesExpected);
		}
	}

	private void load(String fn) throws OWLOntologyCreationException {
		//Injector injector = Guice.createInjector(new ConfigModule());
		OWLLoader loader = new OWLLoader();
		loader.load("src/test/resources/"+fn);
		ontology = loader.createKnowledgeBaseInterface();
		//profileMatcher = 
		//		injector.getInstance(ProfileMatcher.class);
		//profileMatcher = new MaximumInformationContentSimilarityProfileMatcher(ontology);

		profileMatcher = createProfileMatcher(ontology);
	}

	protected abstract ProfileMatcher createProfileMatcher(BMKnowledgeBase kb);

}
