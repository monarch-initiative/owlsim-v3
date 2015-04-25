package org.monarchinitiative.owlsim.compute.matcher;

import org.monarchinitiative.owlsim.model.match.ProfileQuery;

public class TestQuery {
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
