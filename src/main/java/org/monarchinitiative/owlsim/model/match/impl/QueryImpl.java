package org.monarchinitiative.owlsim.model.match.impl;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.model.match.Query;

/**
 * @author cjm
 *
 */
public class QueryImpl implements Query {
	
	private Set<String> queryClassIds;
	
	// TODO: inject this?
	public QueryImpl(Set<String> queryClassIds) {
		super();
		this.queryClassIds = queryClassIds;
	}

	@Deprecated
	public static QueryImpl create(Set<String> labels, LabelMapper labelMapper) throws NonUniqueLabelException {
		Set<String> qids = labelMapper.lookupByUniqueLabels(labels);
		QueryImpl q = new QueryImpl(qids);
		return q;
	}
	
	public static Query create(Set<String> qcids) {
		return new QueryImpl(qcids);
	}
	
	public Set<String> getQueryClassIds() {
		return queryClassIds;
	}

	public void setQueryClassIds(Set<String> queryClassIds) {
		this.queryClassIds = queryClassIds;
	}

	public String toString() {
		return queryClassIds.toString();
	}

	
	
}
