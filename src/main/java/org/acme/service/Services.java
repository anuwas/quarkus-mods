/**
 * 
 */
package org.acme.service;

import org.acme.dto.User;

/**
 * @author ambigo
 * 21-Jan-2026 1:27:50 am
 */
//mark this class as service class
public class Services {
	
	//write a method which will crate a User object and return in quarkus convention
	public User createUser() {
		return new User(1,"Anupam","Biswas");
	}
	

}
