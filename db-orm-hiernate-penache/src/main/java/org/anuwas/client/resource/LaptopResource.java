package org.anuwas.client.resource;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jdk.javadoc.doclet.Doclet;
import org.anuwas.domain.Laptop;

import javax.swing.text.html.Option;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Path("/laptop")
public class LaptopResource {


    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLaptop(Laptop laptop) {
        // Logic to save the laptop to the database
        laptop.persist();
        if(laptop.isPersistent()) {
            return Response.status(Response.Status.CREATED).entity(laptop).build();
            //return Response.created(URI.create("/laptop/"+laptop.id)).build();
        } else {
            // return a 400 Bad Request response if the laptop could not be saved
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

    }

   @Path("/all")
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   public Response getAllLaptops() {
        List<Laptop> laptops = Laptop.listAll();
        return Response.ok(laptops).build();
    }

    @Path("/{id}")
    @GET
    @Produces("application/json")
    public Response getLaptopById(@PathParam("id") Long id) {
        //Optional<Laptop> optionalLapto = Laptop.findByIdOptional(id);
        Laptop laptop = Laptop.findById(id);
        if (laptop != null) {
            return Response.ok(laptop).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
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
