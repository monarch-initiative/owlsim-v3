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
public class GMProfileMatcherABTest extends AbstractGMProfileMatcherTest {

	private Logger LOG = Logger.getLogger(GMProfileMatcherABTest.class);
	
	

	@Test
	public void testA() throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		load("ab-ontology.owl");
		search("ia", 1, 0.1, "a");
	}
	@Test
	public void testB() throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		load("ab-ontology.owl");
		search("ib", 1, 0.1, "b");
	}

	@Test
	public void testAB() throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		// querying with a+b should return the 'gene' that is annotated toboth
		load("ab-ontology.owl");
		search("iab", 1, 0.01, "a","b");
	}
	


}
