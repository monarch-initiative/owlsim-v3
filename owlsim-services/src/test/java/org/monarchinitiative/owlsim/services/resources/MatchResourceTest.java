package org.monarchinitiative.owlsim.services.resources;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.services.exceptions.NonNegatedMatcherException;
import org.monarchinitiative.owlsim.services.exceptions.UnknownMatcherException;

public class MatchResourceTest {

	MatchResource match;

	@Before
	public void setup() {
		match = new MatchResource();
		ProfileMatcher matcher = mock(ProfileMatcher.class);
		NegationAwareProfileMatcher negatedMatcher = mock(NegationAwareProfileMatcher.class);
		match.matchers = new HashMap<>();
		match.matchers.put("foo", matcher);
		match.matchers.put("notfoo", negatedMatcher);
	}

	@Test(expected=UnknownMatcherException.class)
	public void testUnkownMatcher() throws UnknownFilterException, IncoherentStateException {
		match.getMatches("unknown", Collections.<String>emptySet(), Collections.<String>emptySet(), null, null);
	}
	
	@Test(expected=NonNegatedMatcherException.class)
	public void testNegatedIdsWithNonNegatedMatcher() throws UnknownFilterException, IncoherentStateException {
		match.getMatches("foo", Collections.<String>emptySet(), newHashSet("not me"), null, null);
	}
	
	@Test
	public void testNegatedIdsWithNegatedMatcher() throws UnknownFilterException, IncoherentStateException {
		match.getMatches("notfoo", Collections.<String>emptySet(), newHashSet("not me"), null, null);
	}

}
