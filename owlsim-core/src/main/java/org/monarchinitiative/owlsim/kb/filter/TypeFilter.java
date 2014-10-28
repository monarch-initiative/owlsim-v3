package org.monarchinitiative.owlsim.kb.filter;


/**
 * A filter that picks out individuals based on which class(es) that are associated with.
 * You can supply an isExact flag to restrict matching to only the class that is supplied, 
 * otherwise it will filter (by default) on the type including subclasses.
 * You can supply an isNegated flag to invert the match to "not" the given class.
 * 
 * @author cjm, nlw
 *
 */
public class TypeFilter implements Filter  {

	private String typeId;
	private boolean isExact;
	private boolean isNegated;
	
	public TypeFilter(String typeId) {
		this.isExact = false;
		this.isNegated = false;
	}

	public TypeFilter(String typeId, boolean isExact, boolean isNegated) {
		this.isExact = isExact;
		this.isNegated = isNegated;
	}

	
	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public boolean isExact() {
		return isExact;
	}
	
	public void setExact(boolean isExact) {
		this.isExact = isExact;
	}

	public void setNegated(boolean isNegated) {
		this.isNegated = isNegated;
	}
	
	public boolean isNegated() {
		return isNegated;
	}

}
