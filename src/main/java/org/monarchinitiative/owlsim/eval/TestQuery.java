package org.monarchinitiative.owlsim.eval;

import org.monarchinitiative.owlsim.model.match.BasicQuery;

/**
 * A combination of a query an an expected result
 * 
 * @author cjm
 *
 */
public class TestQuery {
	public BasicQuery query;
	public String expectedId;
	public int maxRank = 1;
	
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