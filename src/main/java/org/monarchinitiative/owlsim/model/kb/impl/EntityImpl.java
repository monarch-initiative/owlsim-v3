package org.monarchinitiative.owlsim.model.kb.impl;

import org.monarchinitiative.owlsim.model.kb.Entity;

public class EntityImpl implements Entity {
	private String id;
	private String label;
	
	public EntityImpl(String id, String label) {
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
