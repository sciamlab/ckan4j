package com.sciamlab.ckan4j.utils;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

/**
 * 
 * @author SciamLab
 *
 */

public class SciamlabErrorResponse implements JSONizable{
	
	private static final Logger logger = Logger.getLogger(SciamlabErrorResponse.class);
	
    private int errorCode;
    private String errorMessage;
    private String applicationMessage;


    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getApplicationMessage() {
        return applicationMessage;
    }

    public void setApplicationMessage(String applicationMessage) {
        this.applicationMessage = applicationMessage;
    }
    
    public JSON toJSON() {
		JSONObject json = new JSONObject();
		json.put("code", errorCode);
		json.put("error", errorMessage);
		if(applicationMessage != null && !"".equals(applicationMessage))
			json.put("msg", applicationMessage);
		return json;
	}

	public JSON toJSON(boolean goDeep) {
		return toJSON();
	}
}
