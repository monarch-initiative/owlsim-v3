package org.monarchinitiative.owlsim.services.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.Responses;

public class NonNegatedMatcherException extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	public NonNegatedMatcherException() {
		super(Responses.clientError().build());
	}

	public NonNegatedMatcherException(String matcher) {
		super(Response.status(Responses.CLIENT_ERROR).
				entity(String.format("The matcher \"%s\" can't be used with negated IDs.", matcher)).
				type("text/plain").build());
	}

}
