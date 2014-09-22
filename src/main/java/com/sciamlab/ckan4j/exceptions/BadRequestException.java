package com.sciamlab.ckan4j.exceptions;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

/**
 * 
 * @author SciamLab
 *
 */

public class BadRequestException extends SciamlabWebApplicationException {

	private static final long serialVersionUID = 5212139422947303994L;
	private static final Logger logger = Logger.getLogger(BadRequestException.class);
	private static final Response.Status status = Response.Status.BAD_REQUEST;

	public BadRequestException() {
        this(null);
    }
	
	public BadRequestException(String applicationMessage) {
		super(status.getStatusCode(), status.getStatusCode(), status.getReasonPhrase(), applicationMessage);
    }
}
