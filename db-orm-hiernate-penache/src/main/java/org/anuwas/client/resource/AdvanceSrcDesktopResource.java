package org.anuwas.client.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.anuwas.domain.Desktop;
import org.anuwas.repository.DesktopRepository;

import java.util.List;

@Path("/adv-desktop")
public class AdvanceSrcDesktopResource {

    @Inject
    DesktopRepository    desktopRepository;

    @Path("brand/{brand}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getDesktopByBrand(@PathParam("brand") String brand) {
        List<Desktop> desktops = desktopRepository.list("brand", brand);
        if (desktops != null && !desktops.isEmpty()) {
            return Response.ok(desktops).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/{id}/brand/{brand}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateDesktopByBrandByID(@PathParam("id") Long id, @PathParam("brand") String brand) {
         int updatedCount = desktopRepository.update("brand =?1 where id=?2", brand,id);
        if (updatedCount > 0) {

            return Response.ok("successfully updated").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


}
