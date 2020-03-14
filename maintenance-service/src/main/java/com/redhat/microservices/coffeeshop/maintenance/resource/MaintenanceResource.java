package com.redhat.microservices.coffeeshop.maintenance.resource;

import com.redhat.microservices.coffeeshop.maintenance.model.Maintenance;
import com.redhat.microservices.coffeeshop.maintenance.service.MaintenanceService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/v1")
@Tag(name = "Maintenance resource", description = "Maintenance REST resource")
@ApplicationScoped
public class MaintenanceResource {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceResource.class);

    @Inject
    private MaintenanceService service;

    @POST
    @Path("/maintenance")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a maintenance resource.", operationId = "post_maintenance_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Successful, returning the created resource", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "400", description = "Fail, invalid payload", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Response add(@Context HttpHeaders headers, Maintenance m) {

        if (m == null) {
            return error(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Invalid payload!");
        }

        Response response = null;

        if (service.invalid(headers, m)) {
            response = Response.status(Response.Status.BAD_REQUEST).entity(m).build();
            log.error("Maintenance creation failed");
        }else {
            try {
                m = service.save(m);
                response = Response.status(Response.Status.CREATED).entity(m).build();
                log.info("Maintenance added with id:"+m.getId());
            }catch (Exception e) {
                response = error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        return response;
    }

    @GET
    @Path("/maintenance")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a collection of maintenance resource.", operationId = "get_maintenance_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Successful, returning existing resources", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Maintenance[] getAll(@Context HttpHeaders headers) {
        List<Maintenance> items = service.findAll();

        return items.size() > 0 ? items.toArray(new Maintenance[0]) : null;
    }

    private Response error(Response.Status status, String message) {
        return Response
            .status(status.getStatusCode())
            .entity(Json.createObjectBuilder()
                    .add("error", message)
                    .add("code", status.getStatusCode())
                    .build()
            )
            .build();
    }

}
