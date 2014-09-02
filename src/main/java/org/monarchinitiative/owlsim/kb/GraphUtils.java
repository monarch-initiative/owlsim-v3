package org.monarchinitiative.owlsim.kb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Utilities for {@link BMKnowledgeBase}.
 * 
 * Currently minimal, provides topological sort.
 * 
 * @author cjm
 *
 */
public class GraphUtils {
	
	private Logger LOG = Logger.getLogger(GraphUtils.class);


	/**
	 * Returns a sorted list of class indices such that if x precedes y in the list, then x is not a descendant of y.
	 * 
	 * @param kb
	 * @return sorted list of indices
	 */
	public int[] getTopologicalSort(BMKnowledgeBase kb) {
		Set<Integer> S = new HashSet<Integer>();
		List<Integer> L = new ArrayList<Integer>();
		Map<Integer,Set<Integer>> p2c = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<Integer>> c2p = new HashMap<Integer,Set<Integer>>();
		for (String id : kb.getClassIdsInSignature()) {
			int i = kb.getClassIndex(id);
			if (kb.getStoredDirectSubClassIndex()[i].cardinality() == 0) {
				S.add(i);
			}
			List<Integer> parents = kb.getDirectSuperClassesBM(id).getPositions();
			c2p.put(i, new HashSet<Integer>(parents));
			for (int p : parents) {
				if (!p2c.containsKey(p))
					p2c.put(p, new HashSet<Integer>());
				p2c.get(p).add(i);
			}
		}
		while(!S.isEmpty()){
			Integer n = S.iterator().next();
			S.remove(n);
			
			//LOG.info("  ADDING:"+kb.getClassId(n));
			if (!L.contains(n))
				L.add(n);

			/*
			 * for each node m with an edge e from n to m do
			 * remove edge e from the graph
			 * if m has no other incoming edges then
			 * insert m into S
			 */
			for(Iterator<Integer> it = c2p.get(n).iterator();it.hasNext();){
				Integer m = it.next();
				it.remove();
				p2c.get(m).remove(n);
				if (p2c.get(m).isEmpty()) {
					S.add(m);
				}
			}
		}
		for (Integer p : p2c.keySet()) {
			if (!p2c.get(p).isEmpty()) {
				LOG.error("CYCLE:"+ kb.getClassId(p2c.get(p).iterator().next()));
				return null;
			}
		}
		
		LOG.info("LSIZE="+L.size());
		int[] a = new int[L.size()];
		for (int i=0; i< L.size(); i++) {
			a[i] = L.get(i);
		}
		return a;
	}
}
