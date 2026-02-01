/**
 * 
 */
package org.acme.orm.onetomany;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * @author ambigo
 * 26-Jan-2026 12:32:45 am
 */
@Entity
public class SimCard {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String provider;
	private String phoneNumber;
	
	// Many sim cards can belong to one person, so this is many-to-one relationship
	@ManyToOne
	private Person person;
	

}
