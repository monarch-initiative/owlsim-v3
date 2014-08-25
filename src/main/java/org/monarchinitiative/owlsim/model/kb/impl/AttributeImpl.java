package org.monarchinitiative.owlsim.model.kb.impl;

import org.monarchinitiative.owlsim.model.kb.Attribute;

public class AttributeImpl implements Attribute {
	private String id;
	private String label;
	
	public AttributeImpl(String id, String label) {
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
