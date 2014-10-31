package org.monarchinitiative.owlsim.services.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.Responses;

public class UnknownMatcherException extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	public UnknownMatcherException() {
		super(Responses.notFound().build());
	}

	public UnknownMatcherException(String matcherName) {
		super(Response.status(Responses.NOT_FOUND).
				entity(String.format("The matcher named \"%s\" wasn't registered", matcherName)).
				type("text/plain").build());
	}

}
