package org.monarchinitiative.owlsim.compute.weights;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.WeightedQuery;
import org.monarchinitiative.owlsim.model.match.impl.WeightedQueryImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class SpreadingActivationNetworkUtil {

	protected BMKnowledgeBase knowledgeBase;
	public enum Direction {UP, DOWN};
	
	public SpreadingActivationNetworkUtil(BMKnowledgeBase knowledgeBase) {
		super();
		this.knowledgeBase = knowledgeBase;
	}
	
	public WeightedQuery propagateQuery(ProfileQuery q) {
		if (q instanceof QueryWithNegation)
			return propagateNegatableQuery((QueryWithNegation)q);
		else
			return propagatePositiveQuery(q);
	}
	public WeightedQuery propagatePositiveQuery(ProfileQuery q) {
		return null;
	}
	public WeightedQuery propagateNegatableQuery(QueryWithNegation q) {
		Map<String,Double> m = new HashMap<>();
		for (String cid : q.getQueryClassIds()) {
			m.put(cid, 1.0);
		}
		for (String cid : q.getQueryNegatedClassIds()) {
			m.put(cid, -1.0);
		}
		Set<String> seeds = new HashSet<String>(q.getQueryClassIds());
		unidirectionalPropagate(q.getQueryClassIds(), Direction.UP, m, 0);
		unidirectionalPropagate(seeds, Direction.DOWN, m, 0);
		WeightedQuery wq = 
				WeightedQueryImpl.create(q.getQueryClassIds(), q.getQueryNegatedClassIds(), m);
		return wq;
	}
	public void unidirectionalPropagate(Set<String> nodes, 
			Direction d, Map<String, Double> m, int numHops) {
		EWAHCompressedBitmap nextBM = new EWAHCompressedBitmap();
		for (String n : nodes) {
			EWAHCompressedBitmap bm;
			double sourceScore = m.get(n);
			if (d.equals(Direction.UP))
				bm = knowledgeBase.getDirectSuperClassesBM(n);
			else
				bm = knowledgeBase.getDirectSubClassesBM(n);
			nextBM = nextBM.or(bm);
			for (String c : knowledgeBase.getClassIds(bm)) {
				double s = m.containsKey(c) ? m.get(c) : 0.0;
				s += sourceScore/2;
				m.put(c, s);
			}
		}
		if (nextBM.cardinality() > 0) {
			unidirectionalPropagate(knowledgeBase.getClassIds(nextBM), d, m, numHops+1);
		}

	}


	

}
