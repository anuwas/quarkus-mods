package org.anuwas.client.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.anuwas.domain.Desktop;
import org.anuwas.repository.DesktopRepository;

import java.util.List;

@Path("/desktop")
public class DesktopResource {

    @Inject
    DesktopRepository desktopRepository;

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDesktop(Desktop desktop) {
        // Logic to save the desktop to the database
        desktopRepository.persist(desktop);
        if(desktopRepository.isPersistent(desktop)) {
            return Response.status(Response.Status.CREATED).entity(desktop).build();
            //return Response.created(URI.create("/desktop/"+desktop.id)).build();
        } else {
            // return a 400 Bad Request response if the desktop could not be saved
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

    }

    @Path("/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllDesktops() {
        List<Desktop> desktops = desktopRepository.listAll();
        return Response.ok(desktops).build();
    }

    @Path("/{id}")
    @GET
    @Produces("application/json")
    public Response getDesktopById(@PathParam("id") Long id) {
        //Optional<Desktop> optionalLapto = Desktop.findByIdOptional(id);
        Desktop desktop = desktopRepository.findById(id);
        if (desktop != null) {
            return Response.ok(desktop).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Transactional
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDesktop(@PathParam("id") int id,Desktop desktop) {
        desktopRepository.persist(desktop);
        if(desktopRepository.isPersistent(desktop)) {
            return Response.ok(desktop).build();
        }else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteDesktop(@PathParam("id") long id) {

        Boolean status = desktopRepository.deleteById(id);
        if(status) {
            return Response.noContent().build();
        }else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
