package com.sciamlab.ckan4j.exceptions;

import javax.ws.rs.core.Response;

/**
 * 
 * @author SciamLab
 *
 */

public class InternalServerErrorException extends SciamlabWebApplicationException {

	private static final long serialVersionUID = -7587680786259591935L;

	private static final Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
	
	public InternalServerErrorException() {
        this(null);
    }
	
	public InternalServerErrorException(String applicationMessage) {
		super(status.getStatusCode(), status.getStatusCode(), status.getReasonPhrase(), applicationMessage);
    }

}
