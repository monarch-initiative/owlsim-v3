package org.monarchinitiative.owlsim.model.match.impl;

import java.util.Set;

import org.monarchinitiative.owlsim.model.match.QueryWithNegation;

/**
 * @author cjm
 *
 */
public class QueryWithNegationImpl extends ProfileQueryImpl implements QueryWithNegation {
	
	private Set<String> queryNegatedClassIds;
	

	private QueryWithNegationImpl(Set<String> queryClassIds,
			Set<String> queryNegatedClassIds) {
		super(queryClassIds);
		this.queryNegatedClassIds = queryNegatedClassIds;
	}
	
	public static QueryWithNegation create(
		Set<String> queryClassIds,
		Set<String> queryNegatedClassIds) {
		return new QueryWithNegationImpl(queryClassIds, queryNegatedClassIds);
	}

	public Set<String> getQueryNegatedClassIds() {
		return queryNegatedClassIds;
	}

	public void setQueryNegatedClassIds(Set<String> queryNegatedClassIds) {
		this.queryNegatedClassIds = queryNegatedClassIds;
	}
	
	@Override
	public String toString() {
		return "POS="+getQueryClassIds() + " NEG="+getQueryNegatedClassIds(); 
	}


	
}
