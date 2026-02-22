package org.anuwas.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.anuwas.dto.StudentDTO;
import org.anuwas.service.StudentService;

import java.util.ArrayList;
import java.util.List;

@Path("/students")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StudentResource {

    @Inject
    private StudentService studentService;

    @Path("/all")
    @GET
    public Response getAllStudents() {
       List<StudentDTO> studentDTOList= studentService.getAllStudents();
         return Response.ok(studentDTOList).build();
    }

}
