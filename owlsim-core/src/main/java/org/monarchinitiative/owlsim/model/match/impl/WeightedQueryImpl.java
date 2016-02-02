package org.monarchinitiative.owlsim.model.match.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.WeightedQuery;

/**
 * @author cjm
 *
 */
public class WeightedQueryImpl extends QueryWithNegationImpl implements WeightedQuery {
	

	/**
	 * @return map between classId and weight
	 */
	public Map<String,Double> queryClassWeightMap;

	private WeightedQueryImpl(Set<String> queryClassIds,
			Set<String> queryNegatedClassIds, Map<String, Double> queryClassWeightMap) {
		super(queryClassIds, queryNegatedClassIds);
		this.queryClassWeightMap = queryClassWeightMap;
	}
	
	public static WeightedQuery create(
		Set<String> queryClassIds,
		Set<String> queryNegatedClassIds,
		Map<String,Double> queryClassWeightMap) {
		return new WeightedQueryImpl(queryClassIds, queryNegatedClassIds, queryClassWeightMap);
	}

	/**
	 * @return the queryClassWeightMap
	 */
	public Map<String, Double> getQueryClassWeightMap() {
		return queryClassWeightMap;
	}

	/**
	 * @param queryClassWeightMap the queryClassWeightMap to set
	 */
	public void setQueryClassWeightMap(Map<String, Double> queryClassWeightMap) {
		this.queryClassWeightMap = queryClassWeightMap;
	}

}
