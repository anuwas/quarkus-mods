/**
 * 
 */
package org.acme.orm.onetoone;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * @author ambigo
 * 25-Jan-2026 4:44:55 pm
 */
@Entity
public class Citizen {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String name;
	private String address;
	
	// If we declare mapped by , then no aadhar column will not be created in Citizen table
	// Default fetch type is lazy, but if we want to fetch the value of Aadhar while fetiching value of citizent then we should put Eagaer
	//Cascade all will save both citizent object and Aadhar object, unless we need to save Aadhar value separately
	// @JsonManagedReference this is required to avoid infinite recursion while fetching the citizent object
	@JsonManagedReference
	@OneToOne(mappedBy = "citizen",fetch=FetchType.EAGER,cascade=CascadeType.ALL)
	Aadhar aadhar;

}
