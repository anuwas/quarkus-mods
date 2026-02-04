package org.anuwas;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/hello")
public class GreetingResource {

    @ConfigProperty(name="CEO",defaultValue = "Deafult CEO Value")
    String ceo;

    @ConfigProperty(name="COMMONCEO",defaultValue = "COMMON CEO Shared CEO value")
    String commonCeo;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {

        return ceo + " : " + commonCeo;
    }
}
