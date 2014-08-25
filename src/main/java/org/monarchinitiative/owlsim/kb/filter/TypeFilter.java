package org.monarchinitiative.owlsim.kb.filter;


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
