package org.monarchinitiative.owlsim.model.kb;

import java.util.Objects;

/**
 * @author cjm
 *
 */
public class Attribute implements SimpleObject {

	private final String id;
	private final String label;
	
	public Attribute(String id, String label) {
		this.id = id;
		this.label = label;
	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Attribute attribute = (Attribute) o;
		return Objects.equals(id, attribute.id) &&
				Objects.equals(label, attribute.label);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, label);
	}

	@Override
	public String toString() {
		return "Attribute{" +
				"id='" + id + '\'' +
				", label='" + label + '\'' +
				'}';
	}
}
