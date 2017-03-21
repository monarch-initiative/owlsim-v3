package org.monarchinitiative.owlsim.model.kb;

import java.util.Objects;

/**
 * @author cjm
 *
 */
public class Entity implements SimpleObject {
	
	private final String id;
	private final String label;
	
	public Entity(String id, String label) {
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
		Entity entity = (Entity) o;
		return Objects.equals(id, entity.id) &&
				Objects.equals(label, entity.label);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, label);
	}

	@Override
	public String toString() {
		return "Entity{" +
				"id='" + id + '\'' +
				", label='" + label + '\'' +
				'}';
	}
}
