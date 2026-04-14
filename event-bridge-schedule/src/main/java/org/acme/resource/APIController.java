package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.CreateScheduleRequest;
import org.acme.dto.ScheduleResponse;
import org.acme.service.EventBridgeScheduleService;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class APIController {

    @Inject
    EventBridgeScheduleService scheduleService;

    @POST
    @Path("/create-event")
    public Response createEvent(CreateScheduleRequest request) {
        try {
            ScheduleResponse scheduleResponse = scheduleService.createSchedule(request);
            return Response.ok(scheduleResponse).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
