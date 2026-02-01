/**
 * 
 */
package org.acme;

import org.acme.dto.TVSeries;
import org.acme.service.RestClientproxy;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * @author ambigo
 * 24-Jan-2026 6:12:48 pm
 */
@Path("/restclient")
public class RestClientController {
	
	@RestClient
	RestClientproxy restClientproxy;
	
	// Calling this API - https://api.tvmaze.com/shows/169
	
	@GET
	@Path("/{id}")
	public TVSeries getTVSeriesByID(@PathParam("id") int id) {
		return restClientproxy.getTVSeriesByID(id);
	}

}
