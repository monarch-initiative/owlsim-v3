package org.monarchinitiative.owlsim.model.kb;


/**
 * @author cjm
 *
 */
public class Association {
	
	private Attribute vertex;
	private Double frequency;
	/**
	 * @param vertex
	 * @param frequency
	 */
	public Association(Attribute vertex, Double frequency) {
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
	public void setAttribute(Attribute vertex) {
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
