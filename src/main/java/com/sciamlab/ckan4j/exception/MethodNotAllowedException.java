package com.sciamlab.ckan4j.exception;

import javax.ws.rs.core.Response;

/**
 * User: porter
 * Date: 03/05/2012
 * Time: 12:27
 */
public class MethodNotAllowedException extends SciamlabWebApplicationException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5212139422947303994L;
	
	private static final Response.Status status = Response.Status.METHOD_NOT_ALLOWED;
	
	public MethodNotAllowedException() {
        this(null);
    }
	
	public MethodNotAllowedException(String applicationMessage) {
		super(status.getStatusCode(), status.getStatusCode(), status.getReasonPhrase(), applicationMessage);
    }
}
