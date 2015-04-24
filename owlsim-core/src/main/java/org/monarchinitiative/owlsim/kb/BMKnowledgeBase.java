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
 * 
 * <h4>KB formalism</h4>
 * <ul>
 * <li>A KB is a collection of classes (features) and individuals (entities)
 * <li>Features are connected in a Directed Acyclic Graph (DAG)
 * <li>Each DAG has a single root, called owl:Thing - this is always included
 * <li>Individuals can be described with one or more features. If an individual I is described using C, it is implicitly described by ancestors of C
 * <li>Individuals can be also be described by negating one or more features, i.e. I not(C). If an individual is described using not(C), the this propagates to descendants of C
 * <li>TODO: individuals can have one or more features associated by frequency
 * </ul>
 * 
 * Note that OWLAPI terminology is used (e.g. superclasses), but this may be refactored in future to use neutral DAG terminology (e.g. ancestors)
 * 
 * <h4>Mapping to Bitmap positions</h4>
 * Bitmap vectors are used for fast set-wise operations.
 * 
 * Each class or individual is identified by a String identifier, but behind the scenes,
 * this is mapped to an integer denoting a position in the bitmap. A set of classes or
 * a set of individuals can then be represented by a bitmap.
 * 
 * Note that classes and individuals are mutually exclusive, so the ID to Index mapping
 * is dependent on the datatype
 * 
 * Note: the ordering of bits in the class bitmap is guaranteed to be descending according to
 * frequency of individuals for that term. This means that an iterator starts with classes
 * with the lowest probability (and highest information content).
 *  
 * The JavaEWAH library is used for fast bitmap operations.
 * <h4>Usage notes</h4>
 * Note that it is assumed that the ontology is static - most information is
 * cached in-memory. If the underlying ontology changes, it is currently necessary to
 * create a new KB object - there is no incremental change model
 * 
 * <h4>Implementations</h4>
 * 
 * In the future there may be multiple implementations: Neo4j, JENA. Currently
 * there is only one, a bridge to the OWLAPI, {@link BMKnowledgeBaseOWLAPIImpl}
 * 
 * <h4>Labels and IDs</h4>
 * 
 * A KB uses an internal integer to refer to all objects. For convenience, these can also
 * be referred to by an optional String id, which follows whatever form the input source provides.
 * 
 * A separate {@link LabelMapper} is used to retrieve labels given an ID, or conversely to look
 * up an ID given a label.
 * 
 * 
 * @author cjm
 */
@ImplementedBy(BMKnowledgeBaseOWLAPIImpl.class)
public interface BMKnowledgeBase {
	
	
	/**
	 * Note: there can be >1 class in a node
	 * 
	 * @return set of all class identifiers
	 */
	public Set<String> getClassIdsInSignature();

	/**
	 * Note multiple equivalent classes can form a class node
	 * 
	 * @return size of the kb by class node
	 */
	public int getNumClassNodes();

	/**
	 * Note: there can be >1 individual in a node
	 * 
	 * @return set of all individual identifiers
	 */
	public Set<String> getIndividualIdsInSignature();
	
	/**
	 * @param individualId
	 * @return
	 */
	public int getIndividualIndex(String individualId);

	
	public String getIndividualId(int index);
	
	/**
	 * TODO
	 * 
	 * @param id
	 * @return Attribute class with the id
	 */
	public Attribute getAttribute(String id);

	/**
	 * TODO
	 * 
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
	 * @param classIndex
	 * @return subclasses (direct and direct and equivalent)
	 */
	public EWAHCompressedBitmap getSubClasses(int classIndex);


	/**
	 * @param classIndex
	 * @return direct superclasses of classId as bitmap
	 */
	public EWAHCompressedBitmap getDirectSuperClassesBM(int classIndex);

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
	 * @param classIndex
	 * @return  superclasses (direct and indirect and equivalent) of classId as bitmap
	 */
	public EWAHCompressedBitmap getSuperClassesBM(int classIndex);

	
	/**
	 * @param id - an individual
	 * @return types (direct and indirect) as bitmap
	 */
	public EWAHCompressedBitmap getTypesBM(String id);

	/**
	 * @param id - an individual
	 * @return direct types as bitmap
	 */
	public EWAHCompressedBitmap getDirectTypesBM(String id);
	
	public EWAHCompressedBitmap getNegatedTypesBM(String itemId);

	/**
	 * @param id - individual ID
	 * @param classId - the class with which to filter the classes mapped to the individual ID
	 * @return
	 */
	public EWAHCompressedBitmap getFilteredDirectTypesBM(String id, String classId);

	/**
	 * @param ids - a set of class ids
	 * @param classId - the class with which to filter the class set
	 * @return a bitmap representation of only the original ids tha are subclasses of classId
	 */
	public EWAHCompressedBitmap getFilteredDirectTypesBM(Set<String> ids, String classId);

	
	/**
	 * @param ids - a set of class ids
	 * @param classId - the class with which to filter the class set
	 * @return a bitmap representation of only the original ids tha are subclasses of classId
	 */
	public EWAHCompressedBitmap getFilteredTypesBM(Set<String> ids, String classId);

	
	/**
	 * @return utility object to map labels to ids
	 */
	public LabelMapper getLabelMapper();
	
	/**
	 * Note: each index can correspond to multiple classes c1...cn if this set is an equivalence set.
	 * In this case the *representative* classId is returned
	 * 
	 * @param index
	 * @return classId
	 */
	public String getClassId(int index);

	/**
	 * @param classId
	 * @return integer index for class
	 */
	public int getClassIndex(String classId);

	/**
	 * Return all classIds corresponding to a single index.
	 * 
	 * Note each index corresponds to a single equivalence set. This returns
	 * all members of the equivalence set
	 * 
	 * @param index
	 * @return classIds
	 */
	public Set<String> getClassIds(int index);
	
	/**
	 * Return all classIds corresponding to a bitmap.
	 * 
	 * Note each index corresponds to a single equivalence set. This returns
	 * all members of the equivalence set
	 * 
	 * @param bm
	 * @return classIds
	 */
	public Set<String> getClassIds(EWAHCompressedBitmap bm);
	
	/**
	 * @return array indexed by classIndex yielding the number of individuals per class
	 */
	public int[] getIndividualCountPerClassArray();

	/**
	 * @param classId - an identifier for a class
	 * @return a bitmap representation of only the individuals that (directly or indirectly)
	 *   instantiate classId
	 */
	public EWAHCompressedBitmap getIndividualsBM(String classId);

	/**
	 * @param classIndex - index for a class
	 * @return a bitmap representation of only the individuals that (directly or indirectly)
	 *   instantiate classId
	 */
	public EWAHCompressedBitmap getIndividualsBM(int classIndex);

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

	/**
	 * not advised for general use unless performance is critical
	 * 
	 * @return index keyed by parent yielding child node set as bitmap
	 */
	public EWAHCompressedBitmap[] getStoredDirectSubClassIndex();

	/**
	 * Every kb is guaranteed to have exactly one root index
	 * 
	 * @return root class Id
	 */
	public int getRootIndex();







}
