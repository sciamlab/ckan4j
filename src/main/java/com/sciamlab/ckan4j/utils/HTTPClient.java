package com.sciamlab.ckan4j.utils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;

/**
 * 
 * @author SciamLab
 *
 */

public class HTTPClient {
	
	private static final Logger logger = Logger.getLogger(HTTPClient.class);
	
	private Client c;
	
	public HTTPClient() {
		this.c = ClientBuilder.newClient();
	}

	/**
	 * this method does't set any user agent, header and query params
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Response doGET(URL url) throws IOException {

		return this.doGET(url, null, null);
	}

	/**
	 * this method doesn't set the user agent
	 * 
	 * @param url
	 * @param params
	 * @param header
	 * @return
	 */
	public Response doGET(URL url, MultivaluedMap<String, String> params, MultivaluedMap<String, String> header) {
		return this.doGET(url, null, params, header);
	}
	
	public String doGETString(URL url, String user_agent, MultivaluedMap<String, String> params, MultivaluedMap<String, String> header){
		return doGET(url, user_agent, params, header).readEntity(String.class);
	}
	
	/**
	 * 
	 * this method follows the redirect, if any
	 * 
	 * @param url
	 * @param user_agent
	 * @param params
	 * @param header
	 * @return
	 */
	public Response doGET(URL url, String user_agent, MultivaluedMap<String, String> params, MultivaluedMap<String, String> header){
		return doGET(url, user_agent, true, params, header);
	}
	
	public Response doGET(URL url, String user_agent, boolean follow_redirects, MultivaluedMap<String, String> params, MultivaluedMap<String, String> header){
		
		WebTarget wt = c.target(url.toString()).path("");
		
		if(!follow_redirects)
			wt.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
		
		if(params!=null){
			for(String k : params.keySet()){
				List<String> values = params.get(k);
				for(String v : values){
					wt = wt.queryParam(k, v);
				}
			}
		}
		
		Builder b = wt.request(MediaType.APPLICATION_JSON_TYPE);
		
		if(user_agent!=null)
			b.header("User-Agent", user_agent);
		
		if(header!=null){
			for(String k : header.keySet()){
				List<String> values = header.get(k);
				for(String v : values){
					b.header(k, v);
				}
			}
		}
		return b.get();
	}

	public Response doPOST(URL url, String body, MediaType media_type, MultivaluedMap<String, String> params, MultivaluedMap<String, String> header) {
		
		WebTarget wt = c.target(url.toString()).path("");

		if(params!=null){
			for(String k : params.keySet()){
				List<String> values = params.get(k);
				for(String v : values){
					wt.queryParam(k, v);
				}
			}
		}
		
		Builder b = wt.request();
		
		if(header!=null){
			for(String k : header.keySet()){
				List<String> values = header.get(k);
				for(String v : values){
					b.header(k, v);
				}
			}
		}
		
		return b.post(Entity.entity(body, media_type));
	}
	
	public Response doPUT(URL url, String body, MultivaluedMap<String, String> params, MultivaluedMap<String, String> header) {
		
		WebTarget wt = c.target(url.toString()).path("");
		
		if(params!=null){
			for(String k : params.keySet()){
				List<String> values = params.get(k);
				for(String v : values){
					wt.queryParam(k, v);
				}
			}
		}
		
		Builder b = wt.request(MediaType.APPLICATION_JSON_TYPE);
		
		if(header!=null){
			for(String k : header.keySet()){
				List<String> values = header.get(k);
				for(String v : values){
					b.header(k, v);
				}
			}
		}
		
		return b.put(Entity.entity(body, MediaType.APPLICATION_JSON));
	}
	
	public Response doDELETE(URL url, MultivaluedMap<String, String> params, MultivaluedMap<String, String> header) {
		
		WebTarget wt = c.target(url.toString()).path("");
		
		if(params!=null){
			for(String k : params.keySet()){
				List<String> values = params.get(k);
				for(String v : values){
					wt.queryParam(k, v);
				}
			}
		}
		
		Builder b = wt.request(MediaType.APPLICATION_JSON_TYPE);
				
		if(header!=null){
			for(String k : header.keySet()){
				List<String> values = header.get(k);
				for(String v : values){
					b.header(k, v);
				}
			}
		}
		
		return b.delete();
	}
	
//	public static void main(String[] args) throws Exception {
//		System.out.println(new HTTPClient().shortURL(
//				new URL("http://dati.provincia.fi.it/geonetwork/srv/en/resources.get?uuid=b03f0a0c-1011-474b-878b-8c5c167e9ece&fname=art20.zip&access=private"), 
//				"AIzaSyDRgg3DYov6z3HMBVOZto6fnhDy36bof8U"));
//	}
	
//	public URL shortURL(URL url, final String api_key) throws Exception {
//		JSONObject json = null;
//		String result = null;
//		try {
//			JSONObject body = new JSONObject();
//			body.put("longUrl", url.toString());
//			
//			MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>(){{
//				put("key", new ArrayList<String>(){{ add(api_key);}});
//			}};
//			MultivaluedMap<String, String> header = new MultivaluedHashMap<String, String>(){{
//				put("Content-Type", new ArrayList<String>(){{ add(MediaType.APPLICATION_JSON);}});
//			}};
//			result = this.doPOST(
//					new URL("https://www.googleapis.com/urlshortener/v1/url"), body.toString(), MediaType.APPLICATION_JSON_TYPE, params, header);
//			json = (JSONObject) JSONSerializer.toJSON(result);
//		} catch (Exception e) {
//			logger.error(SciamlabStringUtils.stackTraceToString(e));
//		}
//			if(json!=null && json.containsKey("id")){
//				return new URL(json.getString("id"));
//			}else{
//				throw new Exception(result);
//			}
//	}
	
}
