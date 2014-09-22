package com.sciamlab.ckan4j.exceptions;

import javax.ws.rs.core.Response;

/**
 * 
 * @author SciamLab
 *
 */

public class ForbiddenException extends SciamlabWebApplicationException {

	private static final long serialVersionUID = -7587680786259591935L;

	private static final Response.Status status = Response.Status.FORBIDDEN;
	
	public ForbiddenException() {
        this(null);
    }
	
	public ForbiddenException(String applicationMessage) {
		super(status.getStatusCode(), status.getStatusCode(), status.getReasonPhrase(), applicationMessage);
    }

}
