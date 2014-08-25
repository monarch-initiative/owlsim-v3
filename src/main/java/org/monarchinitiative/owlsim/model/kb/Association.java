package org.monarchinitiative.owlsim.model.kb;

/**
 * An association between an entity and an attribute (for example, between a disease and a phenotype)
 * 
 * @author cjm
 *
 */
public interface Association {
	
	
	/**
	 * @return attribute (class)
	 */
	public Attribute getAttribute();
	
	/**
	 * @return frequence of occurrences of class within type
	 */
	public double getFrequency();

}
