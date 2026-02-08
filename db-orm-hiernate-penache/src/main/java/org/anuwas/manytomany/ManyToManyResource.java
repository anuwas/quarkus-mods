package org.anuwas.manytomany;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.anuwas.manytomany.entity.Bank;
import org.anuwas.manytomany.entity.Dweller;
import org.anuwas.manytomany.repository.BankRepository;
import org.anuwas.manytomany.repository.DwellerRepository;

import java.util.List;
import java.util.stream.Stream;

@Path("/many-to-many")
public class ManyToManyResource {

    @Inject
    DwellerRepository dwellerRepository;

    @Inject
    BankRepository bankRepository;


    @Path("/create-dweller")
    @POST
    @Transactional
    public Response createDweller(Dweller dweller) {
        dwellerRepository.persist(dweller);
        return Response.ok(dweller).build();
    }

    @Path("/create-bank")
    @POST
    @Transactional
    public Response createBank(Bank bank) {
        bankRepository.persist(bank);
        return Response.ok(bank).build();
    }

    @GET
    @Path("/find-all-dweller")
    public Response getDwellerWithBank() {
        // find all dweller with bank details
        List<Dweller> dwellerList = dwellerRepository.listAll();

        return Response.ok(dwellerList).build();
    }

    @GET
    @Path("/find-all-bank")
    public Response getBankWithDweller() {
        // find all bank with dweller details
        List<Bank> bankList = bankRepository.listAll();

        return Response.ok(bankList).build();
    }
}
