package org.monarchinitiative.owlsim.eval;

import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;

/**
 * A combination of a query an an expected result
 * 
 * @author cjm
 *
 */
public class TestQuery {
	public ProfileQuery query;
	public String expectedId;
	public int maxRank = 1;
	public MatchSet matchSet;
	public Integer maxTimeMs = null;
	
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
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TestQuery [query=" + query + ", expectedId=" + expectedId
                + ", maxRank=" + maxRank + ", matchSet=" + matchSet + "]";
    }
	
	
}