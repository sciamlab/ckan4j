package com.sciamlab.ckan4j;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sciamlab.ckan4j.exception.CKANException;
import com.sciamlab.common.exception.web.InternalServerErrorException;
import com.sciamlab.common.exception.web.SciamlabWebApplicationException;
import com.sciamlab.common.util.HTTPClient;

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

public class CKANApiClient implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(CKANApiClient.class);
	
	private final String ckan_api_key;
	private final URL ckan_api_endpoint;
	private final HTTPClient http;
	private final Integer timeout;

	private static final String PACKAGE_LIST = "package_list";
	private static final String GROUP_LIST = "group_list";
	private static final String ORGANIZATION_LIST = "organization_list";
	private static final String TAG_LIST = "tag_list";
	private static final String USER_LIST = "user_list";
	
	private static final String PACKAGE_SHOW = "package_show";
	private static final String RESOURCE_SHOW = "resource_show";
	private static final String GROUP_SHOW = "group_show";
	private static final String ORGANIZATION_SHOW = "organization_show";
	private static final String TAG_SHOW = "tag_show";
	private static final String USER_SHOW = "user_show";
	
	private static final String PACKAGE_UPDATE = "package_update";
	private static final String PACKAGE_CREATE = "package_create";
	private static final String PACKAGE_DELETE = "package_delete";
	private static final String PACKAGE_PURGE  = "package_purge";
	
	private static final String ORGANIZATION_UPDATE = "organization_update";
	private static final String ORGANIZATION_CREATE = "organization_create";
	private static final String ORGANIZATION_DELETE = "organization_delete";
	private static final String ORGANIZATION_PURGE  = "organization_purge";

	private CKANApiClient(CKANApiClientBuilder builder) {
		this.ckan_api_key = builder.ckan_api_key;
		this.ckan_api_endpoint = builder.ckan_api_endpoint;
		this.timeout = builder.timeout;
		http = builder.timeout!=null ? new HTTPClient(builder.timeout) : new HTTPClient();
	}
	
	/*
	 * POST METHODS
	 */
	
	private Object actionPOST(String action, JSONObject body) throws CKANException{
		String result_string = "";
//		System.out.println(html);
		try {
//			HTTPClient http = this.timeout!=null ? new HTTPClient(this.timeout) : new HTTPClient();
			result_string = http.doPOST(new URL(ckan_api_endpoint + "/action/" + action), 
					body.toString(), MediaType.APPLICATION_JSON_TYPE, null, 
					new MultivaluedHashMap<String, String>(){{ 
						put("Authorization", new ArrayList<String>(){{ 
							if(ckan_api_key!=null) add(ckan_api_key); 
						}}); }}).readEntity(String.class);
		} catch (Exception e) {
			throw new CKANException(e);
		}
		JSONObject result = null;
		try {
			result = new JSONObject(result_string);
		} catch (JSONException e) {
			throw new CKANException(e, result_string);
		}
		if(!result.has("success") || !result.getBoolean("success")){
//			System.out.println(result);
			throw new CKANException(result.getJSONObject("error"));
		}
		return result.get("result");
	}
	
	public boolean packageDelete(final String name) throws CKANException{
		JSONObject json = new JSONObject();
		json.put("id", name);
		Object result = actionPOST(PACKAGE_DELETE, json);
		return true;
	}
	
	public boolean packagePurge(final String name) throws CKANException{
		JSONObject json = new JSONObject();
		json.put("id", name);
		Object result = actionPOST(PACKAGE_PURGE, json);
		return true;
	}
	
	public JSONObject packageUpdate(final JSONObject dataset) throws CKANException{
		return (JSONObject) actionPOST(PACKAGE_UPDATE, dataset);
	}
	
	public JSONObject packageCreate(final JSONObject dataset) throws CKANException{
		return (JSONObject) actionPOST(PACKAGE_CREATE, dataset);
	}
	
	public boolean organizationDelete(final String name) throws CKANException{
		JSONObject json = new JSONObject();
		json.put("id", name);
		Object result = actionPOST(ORGANIZATION_DELETE, json);
		logger.debug("Organization successfully deleted : "+name);
		return true;
	}
	
	public boolean organizationPurge(final String name) throws CKANException{
		JSONObject json = new JSONObject();
		json.put("id", name);
		Object result = actionPOST(ORGANIZATION_PURGE, json);
		logger.debug("Organization successfully purged : "+name);
		return true;
	}
	
	/**
	 * name (string) – the name of the organization, a string between 2 and 100 characters long, containing only lowercase alphanumeric characters, - and _
	 * title (string) – the title of the organization (optional)
	 * description (string) – the description of the organization (optional)
	 * image_url (string) – the URL to an image to be displayed on the organization’s page (optional)
	 * extras (list of dataset extra dictionaries) – the org
	 */
	public JSONObject organizationUpdate(final JSONObject organization) throws CKANException{
		return (JSONObject) actionPOST(ORGANIZATION_UPDATE, organization);
	}
	
	/**
	 * name (string) – the name of the organization, a string between 2 and 100 characters long, containing only lowercase alphanumeric characters, - and _
	 * title (string) – the title of the organization (optional)
	 * description (string) – the description of the organization (optional)
	 * image_url (string) – the URL to an image to be displayed on the organization’s page (optional)
	 * extras (list of dataset extra dictionaries) – the org
	 */
	public JSONObject organizationCreate(final JSONObject organization) throws CKANException{
		return (JSONObject) actionPOST(ORGANIZATION_CREATE, organization);
	}
	
	/*
	 * GET METHODS
	 */
	
	public Object actionGET(String action, MultivaluedMap<String, String> params) throws CKANException{
		try {
//			HTTPClient http = this.timeout!=null ? new HTTPClient(this.timeout) : new HTTPClient();
			Response response = http.doGET(new URL(ckan_api_endpoint + "/action/" + action), params,
					new MultivaluedHashMap<String, String>(){{ 
						put("Authorization", new ArrayList<String>(){{ 
							if(ckan_api_key!=null) add(ckan_api_key); 
						}}); }});
			JSONObject result;
			try {
				result = new JSONObject(response.readEntity(String.class));
			} catch (JSONException e) {
				throw new SciamlabWebApplicationException(response.getStatus(), response.getStatus(), response.getStatusInfo().toString(), null);
			}
			if(!result.has("success")){
				//thrown as SciamlabWebApplicationException like :
				/*
				 * {
				 *    error: "Internal Server Error",
				 *    code: 500,
				 *    msg: "bla bla"
				 * } 
				 */
				if(result.has("code") && result.has("error"))
					throw new SciamlabWebApplicationException(result.getInt("code"), result.getInt("code"), result.getString("error"), result.getString("msg"));
				else
					throw new InternalServerErrorException(result.toString());
			}else if(!result.getBoolean("success")){
				//thrown as CKAN internal exception like: 
				/*
				 * {
				 *    help: "http://demo.ckan.org/api/3/action/help_show?name=organization_show",
	             *    success: false,
	             *    error: {
				 *       message: "Not found",
				 * 	    __type: "Not Found Error"
				 * 	  }
				 * } 
				 */
				throw new CKANException(result.getJSONObject("error"));
			}
			return result.get("result");
			
		} catch (CKANException | SciamlabWebApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new CKANException(e);
		}
	}
	
	public JSONArray packageList(final Integer limit, final Integer offset) throws CKANException{
		return (JSONArray) actionGET(PACKAGE_LIST, new MultivaluedHashMap<String, String>(){{ 
				if(limit!=null )put("limit", new ArrayList<String>(){{ add(limit.toString()); }});
				if(offset!=null )put("offset", new ArrayList<String>(){{ add(offset.toString()); }});
			}});
	}
	
	public JSONArray packageList() throws CKANException{
		return packageList(null, null);
	}
	
	public JSONArray groupList() throws CKANException{
		return (JSONArray) actionGET(GROUP_LIST, new MultivaluedHashMap<String, String>());
	}
	
	public JSONArray organizationList() throws CKANException{
		return (JSONArray) actionGET(ORGANIZATION_LIST, new MultivaluedHashMap<String, String>());
	}
	
	public JSONArray tagList() throws CKANException{
		return (JSONArray) actionGET(TAG_LIST, new MultivaluedHashMap<String, String>());
	}	
	
	public JSONArray userList() throws CKANException{
		return (JSONArray) actionGET(USER_LIST, new MultivaluedHashMap<String, String>());
	}
	
	public JSONObject packageShow(final String id) throws CKANException{
		return (JSONObject) actionGET(PACKAGE_SHOW, new MultivaluedHashMap<String, String>(){{ put("id", new ArrayList<String>(){{ add(id); }}); }});
	}
	
	public JSONObject resourceShow(final String id) throws CKANException{
		return (JSONObject) actionGET(RESOURCE_SHOW, new MultivaluedHashMap<String, String>(){{ put("id", new ArrayList<String>(){{ add(id); }}); }});
	}
	
	public JSONObject groupShow(final String id, final Boolean include_dataset) throws CKANException{
		return (JSONObject) actionGET(GROUP_SHOW, new MultivaluedHashMap<String, String>(){{ 
				put("id", new ArrayList<String>(){{ add(id); }}); 
				if(include_dataset!=null && include_dataset)
					put("include_datasets", new ArrayList<String>(){{ add("True"); }});
				else
					put("include_datasets", new ArrayList<String>(){{ add("False"); }});
			}});
	}
	
	public JSONObject organizationShow(final String id, final Boolean include_dataset) throws CKANException{
		return (JSONObject) actionGET(ORGANIZATION_SHOW, new MultivaluedHashMap<String, String>(){{ 
				put("id", new ArrayList<String>(){{ add(id); }}); 
				if(include_dataset!=null && include_dataset)
					put("include_datasets", new ArrayList<String>(){{ add("True"); }});
				else
					put("include_datasets", new ArrayList<String>(){{ add("False"); }});
			}});
	}	
	
	public JSONObject tagShow(final String id) throws CKANException{
		return (JSONObject) actionGET(TAG_SHOW, new MultivaluedHashMap<String, String>(){{ put("id", new ArrayList<String>(){{ add(id); }}); }});
	}	
	
	public JSONObject userShow(final String id) throws CKANException{
		return (JSONObject) actionGET(USER_SHOW, new MultivaluedHashMap<String, String>(){{ put("id", new ArrayList<String>(){{ add(id); }}); }});
	}
	

	public static class CKANApiClientBuilder{
		
		private String ckan_api_key;
		private final URL ckan_api_endpoint;
		private Integer timeout = null;
		
		public static CKANApiClientBuilder init(String ckan_api_endpoint) throws MalformedURLException{
			return new CKANApiClientBuilder(ckan_api_endpoint);
		}
		
		private CKANApiClientBuilder(String ckan_api_endpoint) throws MalformedURLException {
			super();
			this.ckan_api_endpoint = new URL(ckan_api_endpoint);
		}

		public CKANApiClientBuilder apiKey(String ckan_api_key){
			this.ckan_api_key = ckan_api_key;
			return this;
		}
		
		public CKANApiClientBuilder timeout(int timeout){
			this.timeout = timeout;
			return this;
		}
		
		public CKANApiClient build() {
			return new CKANApiClient(this);
		}
	}
}
