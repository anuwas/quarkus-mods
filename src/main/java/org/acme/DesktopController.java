/**
 * 
 */
package org.acme;

import java.net.URI;
import java.util.Optional;

import org.acme.entity.Desktop;
import org.acme.entity.Laptop;
import org.acme.repository.DesktopRepository;

import jakarta.inject.Inject;
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
 * 25-Jan-2026 1:17:49 pm
 */
@Path("/desktop")
public class DesktopController {
	
	@Inject
	DesktopRepository desktopRepository;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLaptopList() {
		return Response.ok(desktopRepository.listAll()).build();
	}
	
	
	@Transactional
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveLaptop(Desktop desktop) {
		desktopRepository.persist(desktop);
		if(desktopRepository.isPersistent(desktop)) {
			return Response.created(URI.create("/desktop/"+desktop.getId())).build();
		}else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLaptop(@PathParam("id") long id) {
		Optional<Desktop> desktop = desktopRepository.findByIdOptional(id);
		if(desktop.isPresent()) {
			return Response.ok(desktop.get()).build();
		}else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
	
	@Transactional
	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateLaptop(@PathParam("id") long id,Desktop desktop) {
		Optional<Desktop> dbDesktopOp = desktopRepository.findByIdOptional(id);
		if(dbDesktopOp.isPresent()) {
			Desktop dbDesktop = dbDesktopOp.get();
			dbDesktop.setBrand(desktop.getBrand());
			dbDesktop.setModel(desktop.getModel());
			desktopRepository.persist(dbDesktop);
			if(desktopRepository.isPersistent(desktop)) {
				return Response.ok(desktop).build();
			}else {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
		}
		return null;
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteLaptop(@PathParam("id") long id) {
		
		Boolean status = desktopRepository.deleteById(id);
		if(status) {
			return Response.noContent().build();
		}else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

}
