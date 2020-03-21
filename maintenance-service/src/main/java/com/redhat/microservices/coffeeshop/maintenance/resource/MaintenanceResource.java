package com.redhat.microservices.coffeeshop.maintenance.resource;

import com.redhat.microservices.coffeeshop.maintenance.model.Greeting;
import com.redhat.microservices.coffeeshop.maintenance.model.Maintenance;
import com.redhat.microservices.coffeeshop.maintenance.service.MaintenanceService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
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
import java.util.Optional;

@Path("/v1")
@Tag(name = "Maintenance resource", description = "Maintenance REST resource")
@ApplicationScoped
public class MaintenanceResource {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceResource.class);

    @Inject
    private MaintenanceService service;

    @Inject
    MetricRegistry metricRegistry;

    @Inject
    @ConfigurationValue("project.stage")
    String stage;

    @GET
    @Path("/environment")
    public Response getStage() {
        return Response.ok("{stage: '" + stage + "'}").build();
    }

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
                // track maintenance in a week (the most common day of week for coffee machine maintenance)
                metricRegistry.counter("maintenance_counter", new org.eclipse.microprofile.metrics.Tag("DAY_OF_WEEK", m.getCreation().getDayOfWeek().name().toUpperCase())).inc();
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

    @Inject
    @ConfigProperty(name = "greeting.message")
    private Optional<String> message;

    @GET
    @Path("/greeting")
    @Produces("application/json")
    public Response greeting(@QueryParam("name") @DefaultValue("World") String name) {
        return message
                .map(s -> Response.ok().entity(new Greeting(String.format(s, name))).build())
                .orElseGet(() -> Response.status(500).entity("ConfigMap not present").build());

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
