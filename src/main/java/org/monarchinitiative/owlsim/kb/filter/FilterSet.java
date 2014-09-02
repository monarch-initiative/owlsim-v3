package org.monarchinitiative.owlsim.kb.filter;

import java.util.List;

/**
 * A conjunctions of filters.
 * 
 * @author cjm
 *
 */
public class FilterSet implements Filter {
	List<Filter> filters;

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	
	
}
