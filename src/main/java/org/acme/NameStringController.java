package org.acme;

import java.util.ArrayList;
import java.util.List;

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

@Path("/uname")
public class NameStringController {
	
	List<String> nameList = new ArrayList<>();

	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getNameList() {
        return Response.ok(nameList).build();
    }
    
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveName(String name) {
    	nameList.add(name);
    	
		return Response.ok(name).build();
	}
    
    /* Note 
     * @PathParam() used for the URL pattern / or get value from URL separated by /
     * @QueryParam() used to get value from URL if the pattern is ? Ex abc.com?name=xxx not with /
     */
    
    // URL for this method - http://localhost:8080/hello/Anupam?newname=Goutam
    
    @PUT
    @Path("/{oldname}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateName(@PathParam("oldname") String oldName, @QueryParam("newname") String newName) {
    	//match and replace with new value from array list name
    	nameList.remove(oldName);
    	nameList.add(newName);
    	
		return Response.ok(newName).build();
	}
    
    
    // URL for this method - http://localhost:8080/hello/Biswas
    
    @DELETE
    @Path("/{deletename}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateName(@PathParam("deletename") String deleteName) {
    	//match and replace with new value from array list name
    	Boolean status = nameList.remove(deleteName);
    	 if(status) {
    		 return Response.ok(deleteName).build();
    	 }else {
    		 return Response.status(Response.Status.BAD_REQUEST).build();
    	 }
	}
}
