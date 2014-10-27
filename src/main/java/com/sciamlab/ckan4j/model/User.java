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

package com.sciamlab.ckan4j.model;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

public class User implements JSONString, Principal{

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String hashedPassword;

    private List<Role> roles = new ArrayList<Role>();

    private String api_key;

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

    public void setApiKey(String api_key) {
    	this.api_key = api_key;
    }
    
    public String getApiKey() {
    	return this.api_key;
    }
    
	@Override
	public String toString() {
		return "User [username="+username+", firstName=" + firstName + ", lastName=" + lastName
				+ ", emailAddress=" + emailAddress + ", hashedPassword="
				+ hashedPassword + ", roles=" + roles + ", api_key=" + api_key + "]";
	}

	@Override
	public String toJSONString() {
		JSONObject result = new JSONObject();
		User user = this;
        result.put("first_name", user.getFirstName());
        result.put("username", user.getUsername());
        result.put("email", user.getEmailAddress());
        result.put("id", user.getId());
		JSONArray roles = new JSONArray();
		for(Role r : user.getRoles()){
			roles.put(r.name());
		}
		result.put("roles", roles);
		return result.toString();
	}


    
    

}
