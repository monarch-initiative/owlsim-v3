package org.monarchinitiative.owlsim.kb.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple filter for individuals by their id
 * The filter stores a set of one or more IDs.
 * @author nicole
 */
public class IdFilter implements Filter {

	Set<String> ids;
	
	public IdFilter(String id) {
		ids = new HashSet<String>();
		ids.add(id);
	}
	
	public IdFilter(Set<String> ids) {
		ids.addAll(ids);
	}
	
	public Set<String> getIds() {
		return this.ids;
	}
}
