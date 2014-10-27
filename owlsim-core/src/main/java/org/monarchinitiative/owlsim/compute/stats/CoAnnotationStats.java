package org.monarchinitiative.owlsim.compute.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

/**
 * This class stores class co-ocurrence / co-annotation information using the Apache Math3 sparse matrix implementation.
 * Subject to the actual use of the data, the sparse matrix implementation might change.
 */
public class CoAnnotationStats {

	/**
	 * The background knowledge base
	 */
	private BMKnowledgeBase knowledgeBase;
	
	/**
	 * The co-annotation matrix is a squared symmetrical matrix holding co-occurence counts
	 */
	private OpenMapRealMatrix coAnnotationMatrix;
	
	public CoAnnotationStats(BMKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
		coAnnotationMatrix = new OpenMapRealMatrix(knowledgeBase.getClassIdsInSignature().size(), knowledgeBase.getClassIdsInSignature().size());
	}
	
	/**
	 * Populates the sparse matrix using all existing individuals.
	 */
	public void populateFullCoAnnotationMatrix() {
		Set<String> individuals = knowledgeBase.getIndividualIdsInSignature();
		
		for (String individualId : individuals) {
			int[] attributes = knowledgeBase.getDirectTypesBM(individualId).toArray();

			for (int i = 0; i < attributes.length - 1; i++) {
				coAnnotationMatrix.addToEntry(attributes[i], attributes[i], 1);
				
				for (int j = i + 1; j < attributes.length; j++) {
					coAnnotationMatrix.addToEntry(attributes[i], attributes[j], 1);
				}
			}
			coAnnotationMatrix.addToEntry(attributes[attributes.length - 1], attributes[attributes.length - 1], 1);
		}
	}
	
	/**
	 * Populates a sparse matrix using a set of given individuals. The return type is 
	 * slightly strange because of the underlying sparse matrix implementation.
	 */
	public double[][] createSubsetCoAnnotationMatrix(Set<String> individualIds) {
		OpenMapRealMatrix subsetCoAnnotationMatrix = new OpenMapRealMatrix(knowledgeBase.getClassIdsInSignature().size(), knowledgeBase.getClassIdsInSignature().size());
		for (String individualId : individualIds) {
			int[] attributes = knowledgeBase.getDirectTypesBM(individualId).toArray();

			for (int i = 0; i < attributes.length - 1; i++) {
				for (int j = i + 1; j < attributes.length; j++) {
					subsetCoAnnotationMatrix.addToEntry(attributes[i], attributes[j], 1);
				}
			}
		}
		
		return subsetCoAnnotationMatrix.getData();
	}

	/**
	 * Returns a list of <Class id - Count> pairs representing all co-annotated classes with the 
	 * given class id.
	 */
	public List<CoAnnotationElement> getCoAnnotatedClassesForAttribute(String classId) {
		return getCoAnnotatedClassesForAttribute(classId, 0);
	}
	
	/**
	 * Same as {@code getCoAnnotatedClassesForAttribute(String classId)}, with the additional constraint
	 * of filtering only those classes that have a count over {@code threshold}
	 */
	public List<CoAnnotationElement> getCoAnnotatedClassesForAttribute(String classId, int threshold) {
		int cutoff = threshold > 0 ? threshold : 0;
		List<CoAnnotationElement> list = new ArrayList<CoAnnotationElement>();
		int classIndex = knowledgeBase.getClassIndex(classId);
		double[] counts = coAnnotationMatrix.getRow(classIndex);
		
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > cutoff) {
				list.add(new CoAnnotationElement(knowledgeBase.getClassId(i), (int) counts[i]));
			}
		}
		
		return list;
	}

	/**
	 * The implementation of {@code getCoAnnotatedClassesForAttribute} extended to individuals, with a provided threshold
	 */
	public Map<String, List<CoAnnotationElement>> getCoAnnotatedClassesForIndividual(String individualId, int threshold) {
		Map<String, List<CoAnnotationElement>> map = new HashMap<String, List<CoAnnotationElement>>();
		
		int[] attributes = knowledgeBase.getDirectTypesBM(individualId).toArray();
		for (int classIndex : attributes) {
			String classId = knowledgeBase.getClassId(classIndex);
			map.put(classId, getCoAnnotatedClassesForAttribute(classId, threshold));
		}
		
		return map;
	}
	
	/**
	 * The implementation of {@code getCoAnnotatedClassesForAttribute} extended to individuals
	 */
	public Map<String, List<CoAnnotationElement>> getCoAnnotatedClassesForIndividual(String individualId) {
		Map<String, List<CoAnnotationElement>> map = new HashMap<String, List<CoAnnotationElement>>();
		
		int[] attributes = knowledgeBase.getDirectTypesBM(individualId).toArray();
		for (int classIndex : attributes) {
			String classId = knowledgeBase.getClassId(classIndex);
			map.put(classId, getCoAnnotatedClassesForAttribute(classId));
		}
		
		return map;
	}
	
	/**
	 * Computes the pairwise Mutual Information of two classes - using their ids
	 */
	public double pairwiseMutualInformation(String classId1, String classId2) {
		int id1_index = knowledgeBase.getClassIndex(classId1);
		int id2_index = knowledgeBase.getClassIndex(classId2);
		return pairwiseMutualInformation(id1_index, id2_index);
	}

	/**
	 * Computes the pairwise Mutual Information of two classes - using their indices
	 */
	public double pairwiseMutualInformation(int classId1_index, int classId2_index) {
		double N_11 = coAnnotationMatrix.getEntry(classId1_index, classId2_index);
		double N = (double) knowledgeBase.getIndividualIdsInSignature().size();
		double N_10 = coAnnotationMatrix.getEntry(classId1_index, classId1_index) - N_11;
		double N_01 = coAnnotationMatrix.getEntry(classId2_index, classId2_index) - N_11;
		double N_00 = N - N_10 - N_01 - N_11;
		
		double t1 = computeMITerm(N_11, N_10 + N_11, N_01 + N_11);
		double t2 = computeMITerm(N_01, N_01 + N_00, N_01 + N_11);
		double t3 = computeMITerm(N_10, N_10 + N_11, N_00 + N_10);
		double t4 = computeMITerm(N_00, N_01 + N_00, N_10 + N_00);

		return 	t1 + t2 + t3 + t4;
	}

	/**
	 * Utility method for computing the Mutual Informantion term:
	 *    (N_Joint / N_Total) * log [(N_Total * N_Joint) / (N_LeftOnly * N_RightOnly)]
	 */
	private double computeMITerm(double jointFrequency, double individualFreqL, double individualFreqR) {
		if (jointFrequency == 0 || individualFreqL == 0 || individualFreqR == 0) {
			return 0.0;
		}
		
		double totalIndividuals = (double) knowledgeBase.getIndividualIdsInSignature().size();
		double frac = jointFrequency / totalIndividuals;
		double logFrac = (totalIndividuals * jointFrequency) / (individualFreqL * individualFreqR); 
		
		return frac * Math.log(logFrac);
	}
}
