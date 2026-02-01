/**
 * 
 */
package org.acme.entity;

import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

/**
 * @author ambigo
 * 25-Jan-2026 1:05:38 am
 */
@Entity
public class Laptop extends PanacheEntity{
	
	private String brand;
	private String model;
	
	//generate getter and setter
	public String getBrand() {
		return brand;
	}	
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	

}
