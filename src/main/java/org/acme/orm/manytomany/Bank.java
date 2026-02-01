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
 * 26-Jan-2026 12:59:34 am
 */
@Entity
public class Bank {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String bankName;
	private String acNumber;
	
	// Due to this mapped by , no separate table will be created for Bank to Publics mapping
	@ManyToMany(mappedBy="banksList")
	List<Publics> publics = new ArrayList<>();

}
