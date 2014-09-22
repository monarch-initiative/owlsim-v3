package org.monarchinitiative.owlsim.model.match;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;

import com.google.inject.ImplementedBy;

/**
 * The most basic type of query, a positive conjunction of features to be matched.  
 * 
 * 
 * @author cjm
 *
 */
@ImplementedBy(ProfileQueryImpl.class)
public interface ProfileQuery {
	
	/**
	 * @return all (positive) class Ids in query
	 */
	public Set<String> getQueryClassIds();
	
	/**
	 * @return query filter
	 */
	public Filter getFilter();
	
	public Integer getLimit();

	public void setLimit(Integer limit);
}
