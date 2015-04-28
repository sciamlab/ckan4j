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

import java.util.Date;

import javax.ws.rs.core.Response;

import com.sciamlab.common.util.SciamlabHashUtils;

/**
 * 
 * @author SciamLab
 *
 */

public class CKANLogin {
	
	private final long time;
	private final boolean remember;
	private final String user;
	private final String secret;
	private final String ckan_endpoint;

	private CKANLogin(CKANLoginBuilder builder) {
		this.time = builder.time;
		this.remember = builder.remember;
		this.user = builder.user;
		this.secret = builder.secret;
		this.ckan_endpoint = builder.ckan_endpoint;
	}

	/**
	 * 
	 * @return the login form
	 */
	public Response login() {
		String password_hashed = SciamlabHashUtils.signStringSHA1(time/1000+user+secret);
		StringBuffer form = new StringBuffer();
		form.append("<html>");
		form.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /></head>");
		form.append("<body onload=\"document.BizPassRedirectForm.submit()\" >");
		form.append("<form name=\"BizPassRedirectForm\" action=\""+ckan_endpoint+"/login_generic?came_from=/user/logged_in\" method=\"post\">");
		form.append("<input type=\"hidden\" id=\"field-login\" type=\"text\" name=\"login\" value=\""+user+"\" placeholder=\"\"  />");
		form.append("<input type=\"hidden\" id=\"field-password\" type=\"password\" name=\"password\" value=\""+time/1000+password_hashed+"\" placeholder=\"\"  />");
		if(remember)
			form.append("<input type=\"hidden\" id=\"field-remember\" type=\"checkbox\" name=\"remember\" value=\"63072000\" checked />");
		form.append("</form>");
		form.append("</body>");
		form.append("</html>");
		return Response.ok(form.toString()).build();
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
		
		private long time;
		private boolean remember = false;
		private final String user;
		private final String secret;
		private final String ckan_endpoint;
		
		public static CKANLoginBuilder getInstance(String user, String secret, String ckan_endpoint){
			return new CKANLoginBuilder(user, secret, ckan_endpoint);
		}
		
		private CKANLoginBuilder(String user, String secret, String ckan_endpoint) {
			super();
			this.user = user;
			this.secret = secret;
			this.ckan_endpoint = ckan_endpoint;
			this.time = new Date().getTime();
		}

		public CKANLoginBuilder time(long time){
			this.time = time;
			return this;
		}
		
		public CKANLoginBuilder remember(boolean remember){
			this.remember = remember;
			return this;
		}

		public CKANLogin build() {
			return new CKANLogin(this);
		}
	}
}
