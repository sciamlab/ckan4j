package com.sciamlab.ckan4j.exceptions;

import javax.ws.rs.core.Response;

/**
 * User: porter
 * Date: 03/05/2012
 * Time: 12:27
 */
public class NotFoundException extends SciamlabWebApplicationException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5212139422947303994L;

	private static final Response.Status status = Response.Status.NOT_FOUND;
	
	public NotFoundException() {
        this(null);
    }
	
	public NotFoundException(String applicationMessage) {
		super(status.getStatusCode(), status.getStatusCode(), status.getReasonPhrase(), applicationMessage);
    }
}
