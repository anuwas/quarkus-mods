package org.anuwas.onetoone;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.anuwas.onetoone.entity.Aadhar;
import org.anuwas.onetoone.entity.Citizen;
import org.anuwas.onetoone.repository.AadharRepository;
import org.anuwas.onetoone.repository.CitizenRepository;

@Path("/onetoone")
public class OneToOneResource {

    @Inject
    CitizenRepository citizenRepository;

    @Inject
    AadharRepository aadharRepository;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createCitizen(Citizen citizen) {
       Aadhar aadhar = new Aadhar();
        aadhar.setAadharNumber("1234567890");
        aadhar.setCompanyName("C");

        citizenRepository.persist(citizen);
        aadhar.setCitizen(citizen);
        aadharRepository.persist(aadhar);
        return Response.ok().build();
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCitizen(@PathParam("id") Long id) {
        Citizen citizen = citizenRepository.findById(id);
        return Response.ok(citizen).build();
    }
}
