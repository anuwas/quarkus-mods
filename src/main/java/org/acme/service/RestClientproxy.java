/**
 * 
 */
package org.acme.service;

import org.acme.dto.TVSeries;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * @author ambigo
 * 24-Jan-2026 6:29:16 pm
 */
@Path("/shows")
@RegisterRestClient(baseUri="https://api.tvmaze.com")
public interface RestClientproxy {
	
	@GET
	@Path("/{id}")
	public TVSeries getTVSeriesByID(@PathParam("id") int id);

}
