package com.sciamlab.ckan4j.auth;

import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ContainerRequest;

import com.sciamlab.ckan4j.dao.CKANDAO;
import com.sciamlab.ckan4j.exception.BadRequestException;
import com.sciamlab.ckan4j.exception.InternalServerErrorException;
import com.sciamlab.ckan4j.model.User;
import com.sciamlab.ckan4j.util.SciamlabStringUtils;

@Priority(Priorities.AUTHENTICATION)
public class ApiKeySecurityFilter implements ContainerRequestFilter {

	private static final Logger logger = Logger.getLogger(ApiKeySecurityFilter.class);
	
	private final CKANDAO dao;
	
	private ApiKeySecurityFilter(ApiKeySecurityContextFilterBuilder builder) { 
    	logger.info("Initializing "+ApiKeySecurityFilter.class.getSimpleName()+"...");
    	this.dao = builder.dao;
    	logger.info("[DONE]");
    }

	@Override
    public void filter(ContainerRequestContext requestContext) {
    	ContainerRequest request = (ContainerRequest) requestContext;
    	String key = request.getHeaderString("Authorization");
        if(key==null){
        	List<String> key_params = request.getUriInfo().getQueryParameters().get("key");
        	if(key_params!=null && !key_params.isEmpty())
        		key = key_params.get(0);
        }
        if(key==null)
        	throw new BadRequestException("Missing Authorization key");
        
        try {
			User user = dao.getUserByApiKey(key);
			request.setSecurityContext(new SecurityContextImpl(user));
		} catch (Exception e) {
			throw new InternalServerErrorException(SciamlabStringUtils.stackTraceToString(e));
		}
		
        return;
    }

	public ContainerRequestFilter getRequestFilter() {
        return this;
    }

    public ContainerResponseFilter getResponseFilter() {
        return null;
    }
    
    public static class ApiKeySecurityContextFilterBuilder{
		
    	private final CKANDAO dao;
		
		public static ApiKeySecurityContextFilterBuilder getInstance(CKANDAO dao){
			return new ApiKeySecurityContextFilterBuilder(dao);
		}
		
		private ApiKeySecurityContextFilterBuilder(CKANDAO dao) {
			super();
			this.dao = dao;
		}

		public ApiKeySecurityFilter build() {
			return new ApiKeySecurityFilter(this);
		}
    }

}
