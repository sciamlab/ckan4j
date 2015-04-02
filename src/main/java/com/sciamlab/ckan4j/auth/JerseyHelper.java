package com.sciamlab.ckan4j.auth;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.AnnotatedMethod;

public class JerseyHelper {
	
	private static final Logger logger = Logger.getLogger(JerseyHelper.class);


	/*
	 * filter used to inject custom implementation of RolesAllowedDynamicFeature.class
	 * The difference between original and custom implementation is in the Exception thrown in case of unauthorized access attempt:
	 * - RolesAllowedDynamicFeature.class throws a ForbiddenException using RolesAllowedRequestFilter private class
	 * - custom implementation throws an ForbiddenException (that extends WebApplicationException) using RolesAllowedCustomFilter
	 */
	public static void registerCustomRolesAllowedDynamicFeature(ResourceConfig rc){
		rc.register(new DynamicFeature() {
			@Override
			public void configure(ResourceInfo resourceInfo, FeatureContext context) {
				AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

				// PermitAll takes precedence over RolesAllowed on the class
		        if (am.isAnnotationPresent(PermitAll.class)) {
		            // Do nothing.
		            return;
		        }
		        
		        // DenyAll on the method take precedence over RolesAllowed and PermitAll
		        if (am.isAnnotationPresent(DenyAll.class)) {
		        	logger.info("["+resourceInfo.getResourceClass().getSimpleName()+"."+resourceInfo.getResourceMethod().getName()+"] ");
		        	context.register(new RolesAllowedSecurityFilter());
		            return;
		        }
		        // RolesAllowed on the method takes precedence over PermitAll
		        RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
		        if (ra != null) {
		        	logger.info("["+resourceInfo.getResourceClass().getSimpleName()+"."+resourceInfo.getResourceMethod().getName()+"] ");
		        	context.register(new RolesAllowedSecurityFilter(ra.value()));
		            return;
		        }
		        
		        // DenyAll can't be attached to classes
		        // RolesAllowed on the class takes precedence over PermitAll
		        ra = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
		        if (ra != null) {
		        	logger.info("["+resourceInfo.getResourceClass().getSimpleName()+"."+resourceInfo.getResourceMethod().getName()+"] ");
		        	context.register(new RolesAllowedSecurityFilter(ra.value()));
		        }
			}
		});
	}

}
