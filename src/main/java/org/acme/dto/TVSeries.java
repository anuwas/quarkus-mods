/**
 * 
 */
package org.acme.dto;

import java.net.URL;
import java.util.List;

/**
 * @author ambigo
 * 24-Jan-2026 6:30:33 pm
 */
public class TVSeries {
	
	private int id;
	private URL url;
	private String name;
	private String type;
	private String language;
	private List<String> genre;
	
	public TVSeries() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public TVSeries(int id, URL url, String name, String type, String language, List<String> genre) {
		super();
		this.id = id;
		this.url = url;
		this.name = name;
		this.type = type;
		this.language = language;
		this.genre = genre;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return the genre
	 */
	public List<String> getGenre() {
		return genre;
	}

	/**
	 * @param genre the genre to set
	 */
	public void setGenre(List<String> genre) {
		this.genre = genre;
	}

	

}
