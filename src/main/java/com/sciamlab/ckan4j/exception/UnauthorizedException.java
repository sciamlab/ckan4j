package com.sciamlab.ckan4j.exception;

import javax.ws.rs.core.Response;


public class UnauthorizedException extends SciamlabWebApplicationException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7587680786259591935L;

	private static final Response.Status status = Response.Status.UNAUTHORIZED;
	
	public UnauthorizedException() {
        this(null);
    }
	
	public UnauthorizedException(String applicationMessage) {
		super(status.getStatusCode(), status.getStatusCode(), status.getReasonPhrase(), applicationMessage);
    }

}
