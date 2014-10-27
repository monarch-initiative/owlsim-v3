package org.monarchinitiative.owlsim.kb.impl;

import org.monarchinitiative.owlsim.kb.CURIEMapper;
import org.semanticweb.owlapi.model.IRI;

/**
 * Partially implemented method for making shortform (CURIEs) from URIs
 * 
 * TODO: complete this
 * 
 * @author cjm
 *
 */
public class CURIEMapperImpl implements CURIEMapper {
	
	/**
	 * @param iri
	 * @return id
	 */
	public String getShortForm(IRI iri) {
		return iri.toString();
	}

	/**
	 * @param id
	 * @return expanded IRI
	 */
	public IRI getExpandedForm(String id) {
		return IRI.create(id);
	}
}
