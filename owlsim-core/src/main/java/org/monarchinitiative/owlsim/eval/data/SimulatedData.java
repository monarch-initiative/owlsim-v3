package org.monarchinitiative.owlsim.eval.data;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * <p>Create derived, synthetic datasets based on sets of ({@link OWLClass}) 
 * attributes represented in EWAHCompressedBitmap arrays.  These can be optionally executed recursively.  
 * Convenience methods allow the user to generate
 * derived data for all or a subset of individuals loaded in a provided {@link BMKnowledgeBase}.
 * </p>
 * <p>
 * Since the data typically doesn't need to be loaded into the knowledgeBase,
 * the results are provided as {@link EWAHCompressedBitmap}[] for fast operations
 * </p>
 * @author nlw
 *
 */
public interface SimulatedData {
		
	/**
	 * <p>A convenience method to create a complete set of annotations based on 
	 * the data previously loaded into the owlsim instance. If recursive methods 
	 * are defined, this will NOT perform them by default. </p>
	 * <p>Based on pre-loaded individuals, a set of derived individuals are created.  
	 * This assumes that recursive methods are NOT used.
	 * For each individual with a set of n attributes, a set of n
	 * individuals will be created.  Each individual will contain one permuted
	 * attribute, where the chosen attribute is replaced.</p>
	 * <p>The Annotations are not loaded into the graph.</p>
	 * <p>Care should be taken when calling this method, as it will return the
	 * full set of annotations based on all individuals in the graph; this could
	 * be very large and produce JavaHeapSpace errors if performed recursively.  
	 * Far less memory is used
	 * by iterating over a set of attributes in the calling function, 
	 * instead of all instances here.</p>
	 * @return A set of {@link EWAHCompressedBitmap} arrays based on the 
	 * instances previously loaded into the owlsim instance.  The Map key
	 * is the individual's bit.
	 * @throws NoReasonerFoundException if no reasoner has been set
	 * @throws UnknownOWLClassException
	 * @see createAnnotationsForAttributeSet
	 */
	public Map<Integer,EWAHCompressedBitmap[]> createAssociations() throws Exception;


	/**
	 * <p>Based on individuals provided, a set of derived individuals are created.  
	 * This assumes that recursive methods are <b>NOT</b> used.
	 * For each individual with a set of n attributes, a set of n
	 * individuals will be created.  Each individual will contain one permuted
	 * attribute, where the chosen attribute is replaced.</p>
	 * Note that these operate on 
	 * {@link HashSets}, and will only retrieve unique attribute sets.</p>
	 * 
	 * @param insts
	 * @return A set of assertion axioms
	 * @throws NoReasonerFoundException if no reasoner has been set.
	 * @throws UnknownOWLClassException
	 * @see createAnnotationsForAttributeSet
	 */
	public Map<Integer,EWAHCompressedBitmap[]> createAssociations(Set<String> individualIds) throws Exception;
		
	/**
	 * <p>Based on sets of attributes provided, a {@link EWAHCompressedBitmap} array of attributes are created.  
	 * Recursive methods will be followed if recursive is set to true.
	 * For each set with a set of n attributes, n sets of annotations will be 
	 * created.  Recursive methods return a flattened set.</p>
	 * <p>Note that these operate on 
	 * {@link HashSets}, and will only retrieve unique attribute sets.</p>
	 * 
	 * @param atts
	 * @return A set of {@link OWLClass} sets
	 * @throws NoReasonerFoundException 
	 * @throws Exception 
	 */
	public EWAHCompressedBitmap[] createAttributeSets(EWAHCompressedBitmap atts) throws Exception ;

	
	/**
	 * Set if the given simulated data methods should be performed
	 * recursively.  Does not apply to all methods.
	 * @param r
	 */
	public void setRecursive(Boolean r);
	
	/**
	 * Figure out the number of combinations that are in a set of
	 * length n when choosing k elements without replacement.
	 * @param n
	 * @param k
	 * @return
	 */
	public int choose(int n, int k);	

	/**
	 * Given an array of {@code EWAHCompressedBitmap} (class sets), this will return {@code n}
	 * randomly selected items in a new array.
	 * @param attributeSets
	 * @param n
	 * @return
	 */
	public EWAHCompressedBitmap[] selectNSubsets(EWAHCompressedBitmap[] attributeSets, int n);

	
}
