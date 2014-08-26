package org.monarchinitiative.owlsim.model.match.impl;

import java.util.Set;

import org.monarchinitiative.owlsim.model.match.QueryWithNegation;

/**
 * @author cjm
 *
 */
public class QueryWithNegationImpl extends BasicQueryImpl implements QueryWithNegation {
	
	private Set<String> queryNegatedClassIds;
	

	public QueryWithNegationImpl(Set<String> queryClassIds,
			Set<String> queryNegatedClassIds) {
		super(queryClassIds);
		this.queryNegatedClassIds = queryNegatedClassIds;
	}

	public Set<String> getQueryNegatedClassIds() {
		return queryNegatedClassIds;
	}

	public void setQueryNegatedClassIds(Set<String> queryNegatedClassIds) {
		this.queryNegatedClassIds = queryNegatedClassIds;
	}
	

	
}
