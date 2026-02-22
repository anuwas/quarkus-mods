package org.anuwas.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class GreetingResource {

    @Path("/hello")
    @GET
    public Response hello() {
        return Response.ok("Hello from Quarkus REST").build();
    }
}
