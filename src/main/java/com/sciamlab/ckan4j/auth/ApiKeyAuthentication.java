package com.sciamlab.ckan4j.auth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.NameBinding;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiKeyAuthentication {
//	public enum AuthenticationType { SIMPLE, OAUTH }
//    AuthenticationType type() default AuthenticationType.SIMPLE;
}