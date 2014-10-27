package org.monarchinitiative.owlsim.compute.stats;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

/**
 * Minimal implementation of KB statistics
 * 
 * TODO - use apache SummaryStatistics
 * 
 * @author cjm
 *
 */
public class KBStatsCalculator {

	private BMKnowledgeBase knowledgeBase;
	
	public KBStatsCalculator(BMKnowledgeBase kb) {
		knowledgeBase = kb;
	}

	public KBStats calculateStats() {
		KBStats s = new KBStats();
		s.numClasses = knowledgeBase.getClassIdsInSignature().size();
		s.numIndividuals = knowledgeBase.getIndividualIdsInSignature().size();
		int totParents = 0;
		int totAncestors = 0;
		for (String id : knowledgeBase.getClassIdsInSignature()) {
			totParents += knowledgeBase.getDirectSuperClassesBM(id).cardinality();
			totAncestors += knowledgeBase.getSuperClassesBM(id).cardinality();
		}
		s.avgParentsPerClass = totParents / (double)s.numClasses;
		s.avgAncestorsPerClass = totAncestors / (double)s.numClasses;
		
		return s;
	}
	
	public class KBStats {
		public int numClasses;
		public int numIndividuals;
		public double avgParentsPerClass;
		public double avgDirectTypesPerIndividual;
		public double avgTypesPerIndividual;
		public double avgAncestorsPerClass;
	}
	
	

	
}
