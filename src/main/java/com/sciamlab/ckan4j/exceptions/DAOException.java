package com.sciamlab.ckan4j.exceptions;

/**
 * 
 * @author SciamLab
 *
 */

public class DAOException extends RuntimeException {

	private static final long serialVersionUID = -2213429135494785501L;

	public DAOException(){
		
	}
	
	public DAOException(String msg){
		super(msg);
	}

	public DAOException(Throwable t){
		super(t);
	}
}
