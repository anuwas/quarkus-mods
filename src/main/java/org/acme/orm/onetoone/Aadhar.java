/**
 * 
 */
package org.acme.orm.onetoone;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * @author ambigo
 * 25-Jan-2026 4:45:04 pm
 */
@Entity
public class Aadhar {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String aadharNumber;
	private String location;
	
	// To avoid infinite recursion while fetching citizent object
	@JsonBackReference
	@OneToOne
	Citizen citizen;
	
	
	

}
