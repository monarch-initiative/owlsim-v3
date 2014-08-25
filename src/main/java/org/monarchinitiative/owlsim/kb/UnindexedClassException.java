package org.monarchinitiative.owlsim.kb;

/**
 * @author cjm
 *
 */
public class UnindexedClassException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 */
	public UnindexedClassException(String id) {
		super("unindexed:" +id);
	}

}
