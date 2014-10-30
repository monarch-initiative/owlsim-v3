package org.monarchinitiative.owlsim.services.resources;

import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;

public class MatchResourceTest {

	MatchResource match;
	ProfileMatcher matcher;

	@Before
	public void setup() {
		match = new MatchResource();
		matcher = mock(ProfileMatcher.class);
		match.matchers = new HashMap<>();
		match.matchers.put("foo", matcher);
	}

	@Test(expected=WebApplicationException.class)
	public void testUnkownMatcher() {
		match.getMatches("unknown", Collections.<String>emptySet());
	}

}
