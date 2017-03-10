package org.monarchinitiative.owlsim.model.match;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.filter.Filter;

/**
 * The most basic type of query, a positive conjunction of features to be
 * matched.
 * 
 * 
 * @author cjm
 *
 */
public interface ProfileQuery {

	/**
	 * @return all (positive) class Ids in query
	 */
	public Set<String> getQueryClassIds();

	/**
	 * @return query filter
	 */
	public Filter getFilter();

	public void setFilter(Filter f);

	public Integer getLimit();

	/**
	 * set to -1 for no limit (all)
	 * 
	 * @param limit
	 */
	public void setLimit(Integer limit);

	/**
	 * an optional set of individuals for which we wish to test ranking.
	 * 
	 * This is for use when we with to limit the number of individuals returned,
	 * but we want to know the ranking and scores of particular individuals
	 * outside the top N
	 * 
	 * @return individual ids
	 */
	public Set<String> getReferenceIndividualIds();

	public void setReferenceIndividualIds(Set<String> indIds);

}
