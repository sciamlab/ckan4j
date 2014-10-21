package com.sciamlab.ckan4j.auth;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import com.sciamlab.ckan4j.exception.ForbiddenException;
import com.sciamlab.ckan4j.model.Role;
import com.sciamlab.ckan4j.model.User;

public class SecurityContextImpl implements SecurityContext {
	
	private static final Logger logger = Logger.getLogger(SecurityContextImpl.class);

    private final User user;

    public SecurityContextImpl(User user) {
        this.user = user;
        logger.info(SecurityContextImpl.class.getSimpleName()+" created for user: "+user.toString());
    }
    
    @Override
    public Principal getUserPrincipal() {
        return user;
    }

    @Override
    public boolean isUserInRole(String role) {
    	if(role.equalsIgnoreCase(Role.anonymous.name()))
             return true;

    	if(user == null)
            throw new ForbiddenException();

    	for(Role r : user.getRoles()){
        	if(r.toString().equalsIgnoreCase(role))
        		return true;
        }
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }
}
