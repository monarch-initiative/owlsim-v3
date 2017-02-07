package org.monarchinitiative.owlsim.kb.ewah;

import java.util.Set;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Bitmap representation and operation over SubClassOf and ClassAssertion axioms.
 * 
 * Operations return Bitmaps. These can be converted to a set of object indices using toArray(),
 * for example:
 * <pre>
 * ancestorClassIndices = ontoEWAH.getSuperClasses(clsIndex).toArray()
 * </pre>
 * 
 * The mapping between Class and Individual objects is handled in a separate bridging layer
 * 
 * @author cmungall@gmail.com
 *
 */
public class EWAHKnowledgeBaseStore {
	
	//private Logger LOG = Logger.getLogger(EWAHKnowledgeBaseStore.class);

	
	private int numberOfClasses;
	private int numberOfIndividuals;
	private EWAHCompressedBitmap[] storedSuperClasses;        // by class
	private EWAHCompressedBitmap[] storedDirectSuperClasses;  // by class
	private EWAHCompressedBitmap[] storedSubClasses;          // by class
	private EWAHCompressedBitmap[] storedDirectSubClasses;    // by class
	private EWAHCompressedBitmap[] storedTypes;               // by ind
	private EWAHCompressedBitmap[] storedDirectTypes;         // by ind
	private EWAHCompressedBitmap[] storedNegatedTypes;        // by ind
	private EWAHCompressedBitmap[] storedDirectNegatedTypes;        // by ind
	private EWAHCompressedBitmap[] storedDirectIndividuals;   // by class
	
	/**
	 * @param numberOfClasses
	 * @param numberOfIndividuals
	 */
	public EWAHKnowledgeBaseStore(int numberOfClasses, int numberOfIndividuals) {
		super();
		this.numberOfClasses = numberOfClasses;
		this.numberOfIndividuals = numberOfIndividuals;
		
		storedSuperClasses = new EWAHCompressedBitmap[numberOfClasses];
		storedDirectSuperClasses = new EWAHCompressedBitmap[numberOfClasses];
		storedSubClasses = new EWAHCompressedBitmap[numberOfClasses];
		storedDirectSubClasses = new EWAHCompressedBitmap[numberOfClasses];
		storedTypes = new EWAHCompressedBitmap[numberOfIndividuals];
		storedDirectTypes = new EWAHCompressedBitmap[numberOfIndividuals];
		storedNegatedTypes =  new EWAHCompressedBitmap[numberOfIndividuals];
		storedDirectNegatedTypes =  new EWAHCompressedBitmap[numberOfIndividuals];
		storedDirectIndividuals =  new EWAHCompressedBitmap[numberOfClasses];
	}

	/**
	 * @param clsIndex
	 * @return all superClasses (direct and indirect) of query class as Bitmap
	 */
	public EWAHCompressedBitmap getSuperClasses(int clsIndex) {
		return storedSuperClasses[clsIndex];
	}

    public EWAHCompressedBitmap getClasses(Set<Integer> clsIndices) {
        EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
        for (int i : clsIndices) {
                bm.set(i);
        }
        return bm;
    }

	/**
	 * @param clsIndex
	 * @param isDirect 
	 * @return all superClasses of query class as Bitmap
	 */
	public EWAHCompressedBitmap getSuperClasses(int clsIndex, boolean isDirect) {
		if (isDirect)
			return storedDirectSuperClasses[clsIndex];
		else
			return storedSuperClasses[clsIndex];
	}

	/**
	 * @param clsIndex
	 * @return all direct superClasses of query class as Bitmap
	 */
	public EWAHCompressedBitmap getDirectSuperClasses(int clsIndex) {
		return storedDirectSuperClasses[clsIndex];
	}
	
	/**
	 * @param clsIndices
	 * @return union of all direct superClasses of all input classes
	 */
	public EWAHCompressedBitmap getDirectSuperClasses(Set<Integer> clsIndices) {
		EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
		for (int i : clsIndices) {
			//TODO: determine why this does not work - may be faster?
			//bm.orToContainer(getSuperClasses(i), bm);
			bm = bm.or(getDirectSuperClasses(i));
		}
		return bm;
	}
	
	/**
	 * @param clsIndices
	 * @return union of all superClasses (direct and indirect) of any input class
	 */
	public EWAHCompressedBitmap getSuperClasses(Set<Integer> clsIndices) {
		EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
		for (int i : clsIndices) {
			//TODO: determine why this does not work - may be faster?
			//bm.orToContainer(getSuperClasses(i), bm);
			bm = bm.or(getSuperClasses(i));
		}
		return bm;
	}
	
	/**
	 * @param clsIndex
	 * @return all direct subClasses of query class as Bitmap
	 */
	public EWAHCompressedBitmap getDirectSubClasses(int clsIndex) {
		return storedDirectSubClasses[clsIndex];
	}

	/**
	 * @param clsIndices
	 * @return union of all subClasses (direct and indirect) of any input class
	 */
	public EWAHCompressedBitmap getSubClasses(Set<Integer> clsIndices) {
		EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
		for (int i : clsIndices) {
			//TODO: determine why this does not work - may be faster?
			//bm.orToContainer(getSuperClasses(i), bm);
			bm = bm.or(getSubClasses(i));
		}
		return bm;
	}

	/**
	 * @param clsIndices
	 * @return union of all direct subClasses of all input classes
	 */
	public EWAHCompressedBitmap getDirectSubClasses(Set<Integer> clsIndices) {
		EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
		for (int i : clsIndices) {
			//TODO: determine why this does not work - may be faster?
			//bm.orToContainer(getSuperClasses(i), bm);
			bm = bm.or(getDirectSubClasses(i));
		}
		return bm;
	}

	/**
	 * @param clsIndex
	 * @return all subClasses (direct and indirect) of query class as Bitmap
	 */
	public EWAHCompressedBitmap getSubClasses(int clsIndex) {
		return storedSubClasses[clsIndex];
	}


	/**
	 * @param individualIndex
	 * @return all classes instantiated by individual (direct and indirect)
	 */
	public EWAHCompressedBitmap getTypes(int individualIndex) {
		return storedTypes[individualIndex];
	}
	
	/**
	 * @param clsIndices
	 * @param classIndex - a class to filter the resulting types
	 * @return all classes (direct and indirect) instantiated by individual, that are subclasses of classIndex
	 */
	public EWAHCompressedBitmap getTypes(Set<Integer> clsIndices, int classIndex) {
		EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
		EWAHCompressedBitmap subclasses = getSubClasses(classIndex);

		for (int i : clsIndices) {
			//TODO: determine why this does not work - may be faster?
			//bm.orToContainer(getSuperClasses(i), bm);
			bm = bm.or(getSuperClasses(i));
		}
		return bm.and(subclasses);

	}
		
	/**
	 * @param individualIndex
	 * @return all classes directly instantiated by individual
	 */
	public EWAHCompressedBitmap getDirectTypes(int individualIndex) {
		return storedDirectTypes[individualIndex];
	}
	

	/**
	 * @param individualIndex
	 * @param classIndex - a class by which to filter the results
	 * @return all classes directly instantiated by individual, that are subclasses of classIndex
	 */
	public EWAHCompressedBitmap getDirectTypes(int individualIndex, int classIndex) {
		EWAHCompressedBitmap filteredDirectTypes = storedDirectTypes[individualIndex];
		EWAHCompressedBitmap subclasses = getSubClasses(classIndex);
		return filteredDirectTypes.and(subclasses);
	}
	
	/**
	 * @param clsIndices - a set of class indices to filter
	 * @param classIndex - a class by which to filter the results
	 * @return all classes directly instantiated by individual, that are subclasses of classIndex
	 */
	public EWAHCompressedBitmap getDirectTypes(Set<Integer> clsIndices, int classIndex) {
		EWAHCompressedBitmap bm = EWAHUtils.convertIndexSetToBitmap(clsIndices);
		EWAHCompressedBitmap subclasses = getSubClasses(classIndex);
		return bm.and(subclasses);
	}

	/**
	 * @param individualIndex
	 * @return all classes negatively associated (directly or indirectly) with individual
	 */
	public EWAHCompressedBitmap getNegatedTypes(int individualIndex) {
		return storedNegatedTypes[individualIndex];
	}
	
	/**
	 * @param individualIndex
	 * @return all classes negatively associated with individual
	 */
	public EWAHCompressedBitmap getDirectNegatedTypes(int individualIndex) {
		return storedDirectNegatedTypes[individualIndex];
	}
	
	
	/**
	 * @param individualIndex
	 * @param isDirect 
	 * @return all classes directly by individual
	 */
	public EWAHCompressedBitmap getTypes(int individualIndex, boolean isDirect) {
		if (isDirect)
			return storedDirectTypes[individualIndex];
		else
			return storedTypes[individualIndex];
	}
	
	/**
	 * @param classIndex
	 * @return all individuals directly asserted to be of the specified type
	 */
	public EWAHCompressedBitmap getDirectIndividuals(int classIndex) {
		return storedDirectIndividuals[classIndex];
	}

	
	/**
	 * Adds stored set of superClasses for a class. This must be called for
	 * all classes prior to calling {@link EWAHKnowledgeBaseStore#getSuperClasses(int clsIndex)}
	 * 
	 * @param clsIndex
	 * @param superClasses
	 */
	public void setSuperClasses(int clsIndex, Set<Integer> superClasses) {
		 storedSuperClasses[clsIndex] = EWAHUtils.convertIndexSetToBitmap(superClasses);
	}

	/**
	 * Adds stored set of direct superClasses for a class. This must be called for
	 * all classes prior to calling {@link EWAHKnowledgeBaseStore#getDirectSuperClasses(int clsIndex)}
	 * 
	 * @param clsIndex
	 * @param superClasses
	 */
	public void setDirectSuperClasses(int clsIndex, Set<Integer> superClasses) {
		 storedDirectSuperClasses[clsIndex] = EWAHUtils.convertIndexSetToBitmap(superClasses);
	}
	
	
	/**
	 * note: not generally advisable to use this index directly,
	 * unless performance is critical
	 * 
	 * @return array indexed by parent yielding child node set bitmap
	 */
	public EWAHCompressedBitmap[] getStoredDirectSubClasses() {
		return storedDirectSubClasses;
	}

	public void setStoredDirectSubClasses(
			EWAHCompressedBitmap[] storedDirectSubClasses) {
		storedDirectSubClasses = storedDirectSubClasses;
	}
	

	/**
	 * @param clsIndex
	 * @param subClasses - direct
	 */
	public void setDirectSubClasses(int clsIndex,
			Set<Integer> subClasses) {
		storedDirectSubClasses[clsIndex] = EWAHUtils.convertIndexSetToBitmap(subClasses);
	}
	
	/**
	 * @param clsIndex
	 * @param subClasses - direct and indirect, including reflexive
	 */
	public void setSubClasses(int clsIndex,
			Set<Integer> subClasses) {
		storedSubClasses[clsIndex] = EWAHUtils.convertIndexSetToBitmap(subClasses);
	}
	
	

	/**
	 * Adds stored set of types (direct and indirect) for an individual.
	 * This must be called for all individuals prior to calling
	 *  {@link EWAHKnowledgeBaseStore#getTypes(int clsIndex)}
	 *  
	 * @param individualIndex 
	 * @param types 
	 */
	public void setTypes(int individualIndex, Set<Integer> types) {
		 storedTypes[individualIndex] = EWAHUtils.convertIndexSetToBitmap(types);
	}

	/**
	 * Adds stored set of direct types  for an individual.
	 * This must be called for all individuals prior to calling
	 *  {@link EWAHKnowledgeBaseStore#getTypes(int clsIndex)}
	 *  
	 * @param individualIndex 
	 * @param types 
	 */
	public void setDirectTypes(int individualIndex, Set<Integer> types) {
		 storedDirectTypes[individualIndex] = EWAHUtils.convertIndexSetToBitmap(types);
	}

	/**
	 * @param individualIndex
	 * @param types
	 */
	public void setNegatedTypes(int individualIndex, Set<Integer> types) {
		 storedNegatedTypes[individualIndex] = EWAHUtils.convertIndexSetToBitmap(types);
	}
	
	/**
	 * @param individualIndex
	 * @param types
	 */
	public void setDirectNegatedTypes(int individualIndex, Set<Integer> types) {
		 storedDirectNegatedTypes[individualIndex] = EWAHUtils.convertIndexSetToBitmap(types);
	}

	
	/**
	 * individuals that directly instantiate a class
	 * 
	 * note we have no corresponding index for direct plus indirect; this
	 * would explode the cache. Instead the caller should iterate through subclasses
	 * calling this
	 * 
	 * @param classIndex
	 * @param individuals
	 */
	public void setDirectIndividuals(int classIndex, Set<Integer> individuals) {
		storedDirectIndividuals[classIndex] = EWAHUtils.convertIndexSetToBitmap(individuals);
	}

	
	@Override
	public String toString() {
		return "OntoEWAH for ontology with "+numberOfClasses+" classes and "+
				numberOfIndividuals+" individuals";
	}

	
}
