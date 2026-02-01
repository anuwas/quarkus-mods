/**
 * 
 */
package org.acme;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.acme.dto.User;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author ambigo
 * 24-Jan-2026 3:26:19 pm
 */
@Path("/uobject")
public class UserObjectController {
	
	public List<User> ueserList = new ArrayList<>();
	
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserList() {
        return Response.ok(ueserList).build();
    }
    
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(User user) {
    	ueserList.add(user);
    	
		return Response.ok(user).build();
	}
    
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateName(@PathParam("id") int id, User newUser) {
    	    	
    	ueserList = ueserList.stream().map(user->{
    		if (user.getId()==id){
    			return newUser;
    		}
    	else {
    		return user;
    	}
    	}).collect(Collectors.toList());
    	
    	
		return Response.ok(ueserList).build();
	}
    
    
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateName(@PathParam("id") int deleteUser) {
    	//match and replace with new value from array list name
    	Optional<User> user = ueserList.stream().filter(u->u.getId()==deleteUser).findFirst();
    	if(user.isPresent()) {
    		ueserList.remove(user);
    		return Response.ok().build();
    	}else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    	
	}

}
