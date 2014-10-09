package com.sciamlab.ckan4j.model;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.language.bm.Rule.RPattern;

import com.sciamlab.ckan4j.utils.JSONizable;
import com.sciamlab.ckan4j.utils.SciamlabHashUtils;

public class User implements JSONizable{

     /**
     * Add additional salt to password hashing
     */
    private static final String HASH_SALT = "d8a8e885-ecce-42bb-8332-894f20f0d8ed";

    private static final int HASH_ITERATIONS = 1000;

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String hashedPassword;

    private List<Role> roles = new ArrayList<Role>();

    private static final int ACCESS_TOKEN_CACHE_SIZE = 10000;

    private String secret;

    public User() {
        this(UUID.randomUUID());
    }

    public User(UUID uuid) {
        addRole(Role.anonymous); //all users are anonymous until credentials are proved
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
    
	public boolean equals(Object otherUser) {
        boolean response = false;

        if(otherUser == null) {
            response = false;
        }
        else if(! (otherUser instanceof User)) {
            response = false;
        }
        else {
            if(((User)otherUser).getId().equals(this.getId())) {
                response = true;
            }
        }

        return response;
    }

    public int hashCode() {
        return getId().hashCode();
    }

    public String getName() {
        return getFirstName() + " " + getLastName();
    }

    public void generateSecret() {
    	if(this.secret==null) 
    		this.secret = UUID.randomUUID().toString();
    }
    
    public void setSecret(String secret) {
    	this.secret = secret;
    }
    
    public String getSecret() {
    	return this.secret;
    }
    
    /**
     * Hash the password using salt values
     * See https://www.owasp.org/index.php/Hashing_Java
     *
     * @param passwordToHash
     * @return hashed password
     */
    public String hashPassword(String passwordToHash) throws Exception {
        return hashToken(passwordToHash, getId().toString() + HASH_SALT );
    }


    private String hashToken(String token, String salt) throws Exception {
        return SciamlabHashUtils.byteToBase64(getHash(HASH_ITERATIONS, token, salt.getBytes()));
    }

    public byte[] getHash(int numberOfIterations, String password, byte[] salt) throws Exception {
       MessageDigest digest = MessageDigest.getInstance("SHA-256");
       digest.reset();
       digest.update(salt);
       byte[] input = digest.digest(password.getBytes("UTF-8"));
       for (int i = 0; i < numberOfIterations; i++) {
           digest.reset();
           input = digest.digest(input);
       }
       return input;
   }

	@Override
	public String toString() {
		return "User [username="+username+", firstName=" + firstName + ", lastName=" + lastName
				+ ", emailAddress=" + emailAddress + ", hashedPassword="
				+ hashedPassword + ", roles=" + roles + ", secret=" + secret + "]";
	}

	@Override
	public JSON toJSON() {
		return toJSON(true);
	}

	@Override
	public JSON toJSON(boolean goDeep) {
		JSONObject result = new JSONObject();
		User user = this;
        result.put("first_name", user.getFirstName());
        result.put("username", user.getUsername());
        result.put("email", user.getEmailAddress());
        result.put("id", user.getId());
		JSONArray roles = new JSONArray();
		for(Role r : user.getRoles()){
			roles.add(r.name());
		}
		result.put("roles", roles);
		return result;
	}


    
    

}
