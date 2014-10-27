package org.monarchinitiative.owlsim.model.match.impl;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;

/**
 * @author cjm
 *
 */
public class ProfileQueryImpl implements ProfileQuery {
	
	private Set<String> queryClassIds;
	private Filter filter;
	private Integer limit;
	
	// TODO: inject this?
	public ProfileQueryImpl(Set<String> queryClassIds) {
		super();
		this.queryClassIds = queryClassIds;
	}

	@Deprecated
	public static ProfileQueryImpl create(Set<String> labels, LabelMapper labelMapper) throws NonUniqueLabelException {
		Set<String> qids = labelMapper.lookupByUniqueLabels(labels);
		ProfileQueryImpl q = new ProfileQueryImpl(qids);
		return q;
	}
	
	public static ProfileQuery create(Set<String> qcids) {
		return new ProfileQueryImpl(qcids);
	}
	
	public Set<String> getQueryClassIds() {
		return queryClassIds;
	}

	public void setQueryClassIds(Set<String> queryClassIds) {
		this.queryClassIds = queryClassIds;
	}
	
	

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@Override
	public String toString() {
		return queryClassIds.toString();
	}

	
	
}
