package com.sciamlab.ckan4j.exception;



public class TooManyRequestsException extends SciamlabWebApplicationException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7587680786259591935L;
	
	public TooManyRequestsException() {
        this(null);
    }
	
	public TooManyRequestsException(String applicationMessage) {
		super(429, 429, "Too Many Requests", applicationMessage);
    }

}
