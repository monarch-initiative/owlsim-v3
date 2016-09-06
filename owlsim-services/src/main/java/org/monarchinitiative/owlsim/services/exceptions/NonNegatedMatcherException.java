package org.monarchinitiative.owlsim.services.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;


public class NonNegatedMatcherException extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	public NonNegatedMatcherException() {
	    super(Response.status(Status.BAD_REQUEST).build());
	}

	public NonNegatedMatcherException(String matcher) {
	    super(Response.status(Status.BAD_REQUEST).
	            entity("This matcher does not support negated IDs: " + matcher).type(MediaType.TEXT_PLAIN).build());
	}

}
