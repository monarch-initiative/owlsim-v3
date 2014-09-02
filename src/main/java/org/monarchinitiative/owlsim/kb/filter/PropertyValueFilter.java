package org.monarchinitiative.owlsim.kb.filter;


/**
 * A filter that picks out individuals that have a particular property set to a particular value.
 * 
 * @author cjm
 *
 */
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
