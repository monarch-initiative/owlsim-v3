package org.monarchinitiative.owlsim.model.kb;

/**
 * @author cjm
 *
 */
public class Attribute implements SimpleObject {

	private String id;
	private String label;
	
	public Attribute(String id, String label) {
		super();
		this.id = id;
		this.label = label;
	}

	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
	
}
