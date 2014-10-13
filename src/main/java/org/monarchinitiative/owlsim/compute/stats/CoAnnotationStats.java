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
				for (int j = i + 1; j < attributes.length; j++) {
					coAnnotationMatrix.addToEntry(attributes[i], attributes[j], 1);
					coAnnotationMatrix.addToEntry(attributes[j], attributes[i], 1);
				}
			}
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
					subsetCoAnnotationMatrix.addToEntry(attributes[j], attributes[i], 1);
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
}
