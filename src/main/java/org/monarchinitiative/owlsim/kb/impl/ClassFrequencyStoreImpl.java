package org.monarchinitiative.owlsim.kb.impl;

import java.util.Map;

/**
 * A store for looking up the number of individuals per class.
 * 
 * @author cjm
 *
 */
public class ClassFrequencyStoreImpl {
	
	private Map<String,Integer> classToFrequencyMap;
	
	/**
	 * @param classId
	 * @return frequency
	 */
	public int getFrequency(String classId) {
		return classToFrequencyMap.get(classId);
	}

	/**
	 * @param classId
	 * @param isStrict
	 * @return frequency or null
	 */
	public Integer getFrequency(String classId, boolean isStrict) {
		if (isStrict)
			return classToFrequencyMap.get(classId);
		else 
			if (classToFrequencyMap.containsKey(classId))
				return classToFrequencyMap.get(classId);
			else
				return null;	
	}

}
