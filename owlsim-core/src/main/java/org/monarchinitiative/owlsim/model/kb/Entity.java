package org.monarchinitiative.owlsim.model.kb;

/**
 * @author cjm
 *
 */
public class Entity implements SimpleObject {
	
	private String id;
	private String label;
	
	public Entity(String id, String label) {
		super();
		this.id = id;
		this.label = label;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
	
}
