package com.sciamlab.ckan4j.auth;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.apache.log4j.Logger;

import com.sciamlab.ckan4j.exception.UnauthorizedException;

@Priority(Priorities.AUTHORIZATION) 
public class RolesAllowedSecurityFilter  implements ContainerRequestFilter {
	private static final Logger logger = Logger.getLogger(RolesAllowedSecurityFilter.class);
	
    private final boolean denyAll;
    private final String[] rolesAllowed;

    public RolesAllowedSecurityFilter() {
    	this(null);
    }

    public RolesAllowedSecurityFilter(String[] rolesAllowed) {
    	logger.info("Initializing RolesAllowedSecurityFilter...");
        this.denyAll = false;
        this.rolesAllowed = (rolesAllowed != null) ? rolesAllowed : new String[] {};
        logger.info("[DONE]");
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!denyAll) {
            for (String role : rolesAllowed) {
                if (requestContext.getSecurityContext().isUserInRole(role)) {
                    return;
                }
            }
        }
//            throw new ForbiddenException();
        throw new UnauthorizedException("Request rejected due to an authorization failure");
    }
}