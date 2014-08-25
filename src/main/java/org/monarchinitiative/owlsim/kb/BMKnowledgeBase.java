package org.monarchinitiative.owlsim.kb;

import java.util.Map;
import java.util.Set;

import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.monarchinitiative.owlsim.model.kb.Attribute;
import org.monarchinitiative.owlsim.model.kb.Entity;

import com.google.inject.ImplementedBy;
import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * An interface to an ontology in which the fundamental unit of representation of 
 * a set of classes or a set of individuals (elements, items) is a Bitmap vector.
 * Bitmap vectors are used for fast set-wise operations.
 * 
 * Each class or individual is identified by a String identifier, but behind the scenes,
 * this is mapped to an integer denoting a position in the bitmap. A set of classes or
 * a set of individuals can then be represented by a bitmap.
 * 
 * Note: the ordering of bits in the class bitmap is guaranteed to be descending according to
 * frequency of individuals for that term. This means that an iterator starts with classes
 * with the lowest probability (and highest information content).
 *  
 * 
 * The JavaEWAH library is used for fast bitmap operations.
 * 
 * Note that it is assumed that the ontology is static - most information is
 * cached in-memory
 * 
 * In the future there may be multiple implementations: Neo4j, JENA. Currently
 * there is only one, a bridge to the OWLAPI
 * 
 * @author cjm
 */
@ImplementedBy(BMKnowledgeBaseOWLAPIImpl.class)
public interface BMKnowledgeBase {
	
	
	/**
	 * @return set of all class identifiers
	 */
	public Set<String> getClassIdsInSignature();

	/**
	 * @return set of all individual identifiers
	 */
	public Set<String> getIndividualIdsInSignature();
	
	/**
	 * @param id
	 * @return Attribute class with the id
	 */
	public Attribute getAttribute(String id);

	/**
	 * @param id
	 * @return Entity individual with the id
	 */
	public Entity getEntity(String id);

	/**
	 * @param classId
	 * @return direct superclasses of classId as bitmap
	 */
	public EWAHCompressedBitmap getDirectSuperClassesBM(String classId);

	/**
	 * @param classId
	 * @return superclasses (direct, indirect and equivalent) of classId as bitmap
	 */
	public EWAHCompressedBitmap getSuperClassesBM(String classId);

	/**
	 * @param classIds
	 * @return union of all superclasses as a bitmap
	 */
	public EWAHCompressedBitmap getSuperClassesBM(Set<String> classIds);
	
	/**
	 * @param id - an individual
	 * @return class set bitmap
	 */
	public EWAHCompressedBitmap getTypesBM(String id);

	
	/**
	 * @return utility object to map labels to ids
	 */
	public LabelMapper getLabelMapper();
	
	/**
	 * Note: each index can correspond to multiple classes c1...cn if this set is an equivalence set.
	 * In this case the representative classId is returned
	 * 
	 * @param index
	 * @return classId
	 */
	public String getClassId(int index);
	
	/**
	 * @return array indexed by classIndex yielding the number of individuals per class
	 */
	public int[] getClassFrequencyArray();

	/**
	 * @param individualId
	 * @return property-value map
	 */
	public Map<String,Set<Object>> getPropertyValueMap(String individualId);
	
	/**
	 * @param individualId
	 * @param property
	 * @return values
	 */
	public Set<Object> getPropertyValues(String individualId, String property);

}
