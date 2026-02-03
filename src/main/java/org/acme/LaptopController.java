/**
 * 
 */
package org.acme;

import java.net.URI;
import java.util.Optional;

import org.acme.entity.Laptop;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author ambigo
 * 25-Jan-2026 1:08:07 am
 */
@Path("/laptop")
public class LaptopController {
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLaptopList() {
		return Response.ok(Laptop.findAll()).build();
	}
	
	@Transactional
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveLaptop(Laptop laptop) {
		Laptop.persist(laptop);
		if(laptop.isPersistent()) {
			return Response.created(URI.create("/laptop/"+laptop.id)).build();
		}else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLaptop(@PathParam("id") int id) {
		Optional<Laptop> laptop = Laptop.findByIdOptional(id);
		if(laptop.isPresent()) {
			return Response.ok(laptop.get()).build();
		}else {
			//return Response.status(Response.Status.BAD_REQUEST).build();
			//return Response.noContent().build();
			//return Response.ok("content message").build();
			return Response.status(Response.Status.BAD_REQUEST).entity("Laptop with id "+id+" not found").build();
		}
	}
	
	@Transactional
	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateLaptop(@PathParam("id") int id,Laptop laptop) {
		Laptop.persist(laptop);
		if(laptop.isPersistent()) {
			return Response.ok(laptop).build();
		}else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteLaptop(@PathParam("id") int id) {
		
		Boolean status = Laptop.deleteById(id);
		if(status) {
			return Response.noContent().build();
		}else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

}
