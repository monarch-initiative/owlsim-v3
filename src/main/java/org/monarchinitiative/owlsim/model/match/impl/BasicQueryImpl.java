package org.monarchinitiative.owlsim.model.match.impl;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.model.match.BasicQuery;

/**
 * @author cjm
 *
 */
public class BasicQueryImpl implements BasicQuery {
	
	private Set<String> queryClassIds;
	
	// TODO: inject this?
	public BasicQueryImpl(Set<String> queryClassIds) {
		super();
		this.queryClassIds = queryClassIds;
	}

	@Deprecated
	public static BasicQueryImpl create(Set<String> labels, LabelMapper labelMapper) throws NonUniqueLabelException {
		Set<String> qids = labelMapper.lookupByUniqueLabels(labels);
		BasicQueryImpl q = new BasicQueryImpl(qids);
		return q;
	}
	
	public static BasicQuery create(Set<String> qcids) {
		return new BasicQueryImpl(qcids);
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
