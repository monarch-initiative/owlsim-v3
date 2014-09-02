package org.monarchinitiative.owlsim.model.match;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.model.match.impl.BasicQueryImpl;

import com.google.inject.ImplementedBy;

/**
 * The most basic type of query, a positive conjunction of features to be matched.  
 * 
 * 
 * @author cjm
 *
 */
@ImplementedBy(BasicQueryImpl.class)
public interface BasicQuery {
	
	/**
	 * @return all (positive) class Ids in query
	 */
	public Set<String> getQueryClassIds();
	
	/**
	 * @return query filter
	 */
	public Filter getFilter();
}
