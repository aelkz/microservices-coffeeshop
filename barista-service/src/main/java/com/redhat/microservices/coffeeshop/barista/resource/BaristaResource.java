package com.redhat.microservices.coffeeshop.barista.resource;

import com.redhat.microservices.coffeeshop.barista.model.Barista;
import com.redhat.microservices.coffeeshop.barista.service.BaristaService;
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
@Tag(name = "Barista resource", description = "Barista REST resource")
@ApplicationScoped
public class BaristaResource {
    private static final Logger log = LoggerFactory.getLogger(BaristaResource.class);

    @Inject
    private BaristaService service;

    @POST
    @Path("/barista")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a barista resource.", operationId = "post_barista_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Successful, returning the created resource", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "400", description = "Fail, invalid payload", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Response add(@Context HttpHeaders headers, Barista b) {

        if (b == null) {
            return error(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Invalid payload!");
        }

        Response response = null;

        if (service.invalid(headers, b)) {
            response = Response.status(Response.Status.BAD_REQUEST).entity(b).build();
            log.error("Barista creation failed");
        }else {
            try {
                b = service.save(b);
                response = Response.status(Response.Status.CREATED).entity(b).build();
                log.info("Barista:"+b.getName()+" added with id:"+b.getId());
            }catch (Exception e) {
                response = error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        return response;
    }

    @GET
    @Path("/barista")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a collection of barista resource.", operationId = "get_barista_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Successful, returning existing resources", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Barista[] getAll(@Context HttpHeaders headers) {
        List<Barista> items = service.findAll();

        return items.size() > 0 ? items.toArray(new Barista[0]) : null;
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
