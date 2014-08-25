package org.monarchinitiative.owlsim.kb.filter;


public class PropertyValueFilter implements Filter  {

	private String propertySymbol;
	private Object filler;
	private boolean isNegated;
	public String getPropertySymbol() {
		return propertySymbol;
	}
	public Object getFiller() {
		return filler;
	}
	public boolean isNegated() {
		return isNegated;
	}
	
	
	

}
