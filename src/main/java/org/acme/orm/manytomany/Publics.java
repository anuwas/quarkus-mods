/**
 * 
 */
package org.acme.orm.manytomany;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

/**
 * @author ambigo
 * 26-Jan-2026 12:59:27 am
 */
@Entity
public class Publics {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String name;
	private String address;
	
	@ManyToMany
	List<Bank> banksList = new ArrayList<>();

}
