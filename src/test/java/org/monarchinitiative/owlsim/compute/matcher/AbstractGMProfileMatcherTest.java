package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaivesBayesFixedWeightProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.GMProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.GMProfileMatcher.GMProfileMatcherConfig;
import org.monarchinitiative.owlsim.compute.matcher.impl.GridProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.JaccardSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.MaximumInformationContentSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
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
public class AbstractGMProfileMatcherTest extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(AbstractGMProfileMatcherTest.class);

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
		evaluateTestQuery(profileMatcher, tq);
	}	







}
