package org.monarchinitiative.owlsim.model.match;

import java.util.Set;

/**
 * A query that contains at least a set of positive classes (features) and
 * negative/NOT classes
 * 
 * @author cjm
 *
 */
public interface QueryWithNegation extends Query {

	/**
	 * @return negative features
	 */
	public Set<String> getQueryNegatedClassIds();

}
