/**
 * Copyright 2014 Sciamlab s.r.l.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
