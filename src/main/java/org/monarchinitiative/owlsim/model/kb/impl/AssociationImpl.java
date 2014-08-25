package org.monarchinitiative.owlsim.model.kb.impl;

import org.monarchinitiative.owlsim.model.kb.Association;
import org.monarchinitiative.owlsim.model.kb.Attribute;

/**
 * @author cjm
 *
 */
public class AssociationImpl implements Association {
	
	private AttributeImpl vertex;
	private Double frequency;
	/**
	 * @param vertex
	 * @param frequency
	 */
	public AssociationImpl(AttributeImpl vertex, Double frequency) {
		super();
		this.vertex = vertex;
		this.frequency = frequency;
	}
	/**
	 * @return
	 */
	public Attribute getAttribute() {
		return vertex;
	}
	/**
	 * @param vertex
	 */
	public void setAttribute(AttributeImpl vertex) {
		this.vertex = vertex;
	}
	/**
	 * @return frequency of occurrence (1.0 if undefined)
	 */
	public double getFrequency() {
		if (frequency == null)
			return 1.0;
		return frequency;
	}
	/**
	 * @param frequency
	 */
	public void setFrequency(Double frequency) {
		this.frequency = frequency;
	};
	
	

}
