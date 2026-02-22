package org.anuwas.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.anuwas.dto.EmployeeDTO;
import org.anuwas.repository.EmployeeRepository;

import java.util.List;

@Path("/employee")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    @Inject
    EmployeeRepository employeeRepository;

    @Path("/all")
    @GET
    public Response showAllEmployees() {
        List<EmployeeDTO> employeeDTOList = employeeRepository.getEmplyoeeList();
        return Response.ok(employeeDTOList).build();
    }

    @Path("/{id}")
    @GET
    public Response getEmployeeById(Long id) {
        EmployeeDTO employeeDTO = employeeRepository.getEmployeeById(id);
        return Response.ok(employeeDTO).build();
    }

    @Path("/create")
    @POST
    public Response createEmployee(EmployeeDTO employeeDTO) {
        EmployeeDTO createdEmployee = employeeRepository.createEmployee(employeeDTO);
        return Response.ok(createdEmployee).build();
    }

}
