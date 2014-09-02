package org.monarchinitiative.owlsim.kb;

import org.semanticweb.owlapi.model.IRI;

/**
 * Maps URIs/IRIs onto shortform IDs.
 * 
 * @author cjm
 *
 */
public interface CURIEMapper {

	/**
	 * @param iri
	 * @return Shorform (aka ID, CURIE)
	 */
	public String getShortForm(IRI iri);

	/**
	 * @param id
	 * @return expanded IRI
	 */
	public IRI getExpandedForm(String id);
}
