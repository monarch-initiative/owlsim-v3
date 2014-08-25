package org.monarchinitiative.owlsim.kb.impl;

import java.util.Map;

/**
 * A store for looking up the number of individuals per class
 * 
 * @author cjm
 *
 */
public class ClassFrequenceStoreImpl {
	
	private Map<String,Integer> classToFrequenceMa;
	
	/**
	 * @param classId
	 * @return frequency
	 */
	public int getFrequency(String classId) {
		return classToFrequenceMa.get(classId);
	}

	/**
	 * @param classId
	 * @param isStrict
	 * @return frequency or null
	 */
	public Integer getFrequency(String classId, boolean isStrict) {
		if (isStrict)
			return classToFrequenceMa.get(classId);
		else 
			if (classToFrequenceMa.containsKey(classId))
				return classToFrequenceMa.get(classId);
			else
				return null;	
	}

}
