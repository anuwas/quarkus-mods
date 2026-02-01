package net.anuwas.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.anuwas.entity.Student;
import net.anuwas.exception.BusinessException;
import net.anuwas.exception.TechnicalException;
import net.anuwas.repository.StudentRepository;

import java.net.URI;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentResource {

    @Inject
    StudentRepository  studentRepository;

    /*
    @Path("add-student")
    @POST
    @Transactional
    public Response addStudent(Student student) {
        studentRepository.persist(student);
        if(studentRepository.isPersistent(student)){
            return Response.created(URI.create("/student/" + student.getStudentId())).build();
        }else{
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

     */
    // same post method with exception handling
    @Path("add-student")
    @POST
    @Transactional
    public Response addStudent(Student student) throws BusinessException {
        if(student.getName() == null || student.getName().isEmpty()){
            throw new BusinessException(Response.Status.BAD_REQUEST.getStatusCode(), "Student name cannot be empty");
        }
        studentRepository.persist(student);
        if (studentRepository.isPersistent(student)) {
            return Response.created(URI.create("/student/" + student.getStudentId())).build();
        } else {
            throw new BusinessException(Response.Status.BAD_REQUEST.getStatusCode(), "Failed to add student");
        }
    }
    /* This is a normal funcation without exception handling
    @GET
    @Path("student/{id}")
    @Transactional
    public Response getStudentById(@PathParam("id") Long id) {
        Student student = studentRepository.findById(id);
        if (student == null)
            return Response.ok(Response.status(Response.Status.NOT_FOUND)).build();
        else
            return Response.ok(student).build();
    }
    */

    // same get method with exception handling
    @GET
    @Path("student/{id}")
    @Transactional
    public Response getStudentById(@PathParam("id") Long id) throws BusinessException {
        Student student = studentRepository.findById(id);
        if (student == null)
            throw new BusinessException(Response.Status.NOT_FOUND.getStatusCode(), "Student with id " + id + " not found");
        else
            return Response.ok(student).build();
    }


    @GET
    @Path("getall-students")
    @Transactional
    public Response getStudentList() {
        return Response.ok(studentRepository.listAll()).build();
    }

    @GET
    @Path("divide/{i}")
    @Transactional
    public Response testApi(@PathParam("i") int i) throws TechnicalException {
        try{
            int out = 8 / i;
            return Response.ok("Output after dividing " + out).build();
        }catch (Exception e){
            throw new TechnicalException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    e.getMessage());

        }
    }
}
