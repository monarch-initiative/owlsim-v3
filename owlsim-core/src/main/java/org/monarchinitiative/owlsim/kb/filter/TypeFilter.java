package org.monarchinitiative.owlsim.kb.filter;


/**
 * A filter that picks out individuals based on which class(es) that are associated with.
 * 
 * @author cjm
 *
 */
public class TypeFilter implements Filter  {

	private String typeId;
	private boolean isNegated;

	
	
	public String getTypeId() {
		return typeId;
	}



	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}



	public boolean isNegated() {
		return isNegated;
	}
	
	
	

}
