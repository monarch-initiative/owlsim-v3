package org.monarchinitiative.owlsim.kb;

import java.util.Set;

/**
 * Maps CURIE strings to labels and vice versa.
 * 
 * @author cjm
 *
 */
public interface LabelMapper {

	/**
	 * @param label
	 * @return ids
	 */
	public Set<String> lookupByLabel(String label);
	
	/**
	 * @param label
	 * @return id that has this label
	 * @throws NonUniqueLabelException
	 */
	public String  lookupByUniqueLabel(String label) throws NonUniqueLabelException;
	
	/**
	 * @param labels
	 * @return id that has this labels
	 * @throws NonUniqueLabelException
	 */
	public Set<String> lookupByUniqueLabels(Set<String> labels) throws NonUniqueLabelException;
	
	/**
	 * @param id
	 * @return labels
	 */
	public Set<String> getLabel(String id);
	
	/**
	 * @param id
	 * @return unique label for this id
	 * @throws NonUniqueLabelException
	 */
	public String  getUniqueLabel(String id) throws NonUniqueLabelException;

	/**
	 * @param id
	 * @return label for this id. if label is not unique, an arbitrary one is selected
	 */
	public String  getArbitraryLabel(String id);
}
