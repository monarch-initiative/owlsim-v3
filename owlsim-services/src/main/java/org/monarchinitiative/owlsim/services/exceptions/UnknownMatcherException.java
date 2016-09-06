package org.monarchinitiative.owlsim.services.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class UnknownMatcherException extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	public UnknownMatcherException() {
        super(Response.status(Status.BAD_REQUEST).build());
	}

	public UnknownMatcherException(String matcherName) {
        super(Response.status(Status.BAD_REQUEST).
                entity("This matcher not registered: " + matcherName).type(MediaType.TEXT_PLAIN).build());
	}

}
