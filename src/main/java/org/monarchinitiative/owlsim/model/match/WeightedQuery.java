package org.monarchinitiative.owlsim.model.match;

import java.util.Map;

/**
 * A query in which all features (query classes) are weighted
 * 
 * TODO: define scale, distinguish certaintly from negation?
 * 
 * @author cjm
 *
 */
public interface WeightedQuery extends Query {

	/**
	 * @return map between classId and weight
	 */
	public Map<String,Double> getQueryClassWeightMap();

}
