package org.anuwas.onetomany;

import jakarta.inject.Inject;
import jakarta.persistence.Column;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.anuwas.onetomany.entity.Resident;
import org.anuwas.onetomany.entity.SimCard;
import org.anuwas.onetomany.repository.ResidentRepository;
import org.anuwas.onetomany.repository.SimCardRepository;

import java.util.ArrayList;
import java.util.List;

@Path("/one-to-many")
public class OneToManyResource {

    @Inject
    ResidentRepository residentRepository;

    @Inject
    SimCardRepository simCardRepository;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createResidentWithSimCard(Resident resident) {
        SimCard s = new SimCard();
        s.setSimCardNumber("1234567890");
        s.setProvider("A");
        s.setResident(resident);

        SimCard s1 = new SimCard();
        s1.setSimCardNumber("0987654321");
        s1.setProvider("B");
        s1.setResident(resident);

        List<SimCard> simCards = new ArrayList<>();
        simCards.add(s);
        simCards.add(s1);
        resident.setSimCard(simCards);
        residentRepository.persist(resident);
        return Response.ok("Resident with SimCard created successfully").build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getResidentWithSimCard(@PathParam("id") Long id) {
        Resident resident = residentRepository.findById(id);
        return Response.ok(resident).build();
    }

    @GET
    @Path("/sim/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSimCardWithResident(@PathParam("id") Long id) {
        SimCard simCard = simCardRepository.findById(id);
        return Response.ok(simCard).build();
    }
}
