package org.monarchinitiative.owlsim.kb.filter;


/**
 * A filter that picks out individuals that with a particular prefix.
 * 
 * If URIs are already mapped to CURIEs, use the prefix plus ':'
 * 
 * If URIs are not mapped, use the full URI prefix
 * 
 * @author cjm
 *
 */
public class IdPrefixFilter implements Filter  {

	private String prefix;
	

	public IdPrefixFilter(String prefix) {
		super();
		this.prefix = prefix;
	}
	public static IdPrefixFilter create(String prefix) {
		return new IdPrefixFilter(prefix);
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	

}
