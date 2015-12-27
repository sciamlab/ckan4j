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
package com.sciamlab.ckan4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.ws.rs.core.Response;

import com.sciamlab.common.util.SciamlabHashUtils;

/**
 * 
 * @author SciamLab
 *
 */

public class CKANLogin {

	private final String secret;
	private final URL ckan_endpoint;

	private CKANLogin(CKANLoginBuilder builder) {
		this.secret = builder.secret;
		this.ckan_endpoint = builder.ckan_endpoint;
	}

	/**
	 * 
	 * @param user
	 * @param password
	 * @return the login form
	 */
	public Response login(String user, String password) {
		return this.login(user, password, false);
	}
	
	/**
	 * 
	 * @param user
	 * @return the auto login form
	 */
	public Response autologin(String user) {
		return this.autologin(user, false);
	}
	
	/**
	 * 
	 * @param user
	 * @param password
	 * @param remember
	 * @return the login form
	 */
	public Response login(String user, String password, boolean remember) {
		StringBuffer form = new StringBuffer();
		form.append("<html>");
		form.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /></head>");
		form.append("<body onload=\"document.BizPassRedirectForm.submit()\" >");
		form.append("<form name=\"BizPassRedirectForm\" action=\""+ckan_endpoint+"/login_generic?came_from=/user/logged_in\" method=\"post\">");
		form.append("<input type=\"hidden\" id=\"field-login\" type=\"text\" name=\"login\" value=\""+user+"\" placeholder=\"\"  />");
		form.append("<input type=\"hidden\" id=\"field-password\" type=\"password\" name=\"password\" value=\""+password+"\" placeholder=\"\"  />");
		if(remember)
			form.append("<input type=\"hidden\" id=\"field-remember\" type=\"checkbox\" name=\"remember\" value=\"63072000\" checked />");
		form.append("</form>");
		form.append("</body>");
		form.append("</html>");
		return Response.ok(form.toString()).build();
	}
	
	/**
	 * 
	 * @param user
	 * @param remember
	 * @return the auto login form
	 */
	public Response autologin(String user, boolean remember) {
		Date now = new Date();
		String password_hashed = SciamlabHashUtils.sha1base64((now.getTime()/1000+user+secret));
		return login(user, now.getTime()/1000+password_hashed, remember);
	}
	
	/**
	 * 
	 * @return the logout form
	 */
	public Response logout(){
		StringBuffer buf = new StringBuffer();
		buf.append("<html>");
		buf.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /></head>");
		buf.append("<body onload=\"document.BizPassRedirectForm.submit()\" >");
		buf.append("<form name=\"BizPassRedirectForm\" action=\""+ckan_endpoint+"/user/_logout\" method=\"get\">");
		buf.append("</form>");
		buf.append("</body>");
		buf.append("</html>");
		return Response.ok(buf.toString()).build();
	}
	
	public static class CKANLoginBuilder{
		
		private String secret;
		private final URL ckan_endpoint;
		
		/**
		 * 
		 * @param ckan_endpoint
		 * @return the CKANLoginBuilder instance
		 * @throws MalformedURLException 
		 */
		public static CKANLoginBuilder getInstance(String ckan_endpoint) throws MalformedURLException{
			return new CKANLoginBuilder(ckan_endpoint);
		}
		
		private CKANLoginBuilder(String ckan_endpoint) throws MalformedURLException {
			super();
			this.ckan_endpoint = new URL(ckan_endpoint);
		}
		
		/**
		 * set the secret parameter used to get autologin form in ckan 
		 * @param secret
		 * @return the CKANLoginBuilder instance
		 */
		public CKANLoginBuilder secret(String secret){
			this.secret = secret;
			return this;
		}

		public CKANLogin build() {
			return new CKANLogin(this);
		}
	}
}
