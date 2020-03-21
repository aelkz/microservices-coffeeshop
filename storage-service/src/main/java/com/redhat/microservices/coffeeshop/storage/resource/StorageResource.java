package com.redhat.microservices.coffeeshop.storage.resource;

import com.redhat.microservices.coffeeshop.storage.model.Storage;
import com.redhat.microservices.coffeeshop.storage.repository.StorageRepository;
import com.redhat.microservices.coffeeshop.storage.service.StorageService;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
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

@Path("/v1")
@Tag(name = "Storage resource", description = "Storage REST resource")
@ApplicationScoped
public class StorageResource {
    private static final Logger log = LoggerFactory.getLogger(StorageRepository.class);

    private Double currentCoffeeStorage;

    private Double currentMilkStorage;

    @Inject
    private StorageService service;

    // concurrent gauge to allow tracking current coffee storage
    @Gauge(unit = MetricUnits.NONE, name = "coffee_storage_gauge", absolute = true)
    public Double getCurrentCoffeeStorage() {
        return currentCoffeeStorage;
    }

    // concurrent gauge to allow tracking current milk storage
    @Gauge(unit = MetricUnits.NONE, name = "milk_storage_gauge", absolute = true)
    public Double getCurrentMilkStorage() {
        return currentMilkStorage;
    }

    @Inject
    @ConfigurationValue("project.stage")
    String stage;

    @GET
    @Path("/environment")
    public Response getStage() {
        return Response.ok("{stage: '" + stage + "'}").build();
    }

    @POST
    @Path("/storage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a storage resource.", operationId = "post_storage_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Successful, returning the created resource", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "400", description = "Fail, invalid payload", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Response maintain(@Context HttpHeaders headers, Storage s) {
        if (s == null) {
            return error(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Invalid payload!");
        }

        Response response = null;

        if (service.invalid(headers, s)) {
            response = Response.status(Response.Status.BAD_REQUEST).entity(s).build();
            log.error("Storage creation failed");
        }else {
            try {
                s = service.save(s);
                response = Response.status(Response.Status.CREATED).entity(s).build();
                log.info("Storage record added with id:"+s.getId());
                setCurrentCoffeeStorage(s.getTotalCoffee());
                setCurrentMilkStorage(s.getTotalMilk());
            }catch (Exception e) {
                response = error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        return response;
    }

    @PUT
    @Path("/storage")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will update a storage resource.", operationId = "put_storage_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Successful, without response body", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "400", description = "Fail, invalid payload", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Response updateMaintainanceRecord(@Context HttpHeaders headers, Storage s) {
        if (s == null) {
            return error(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Invalid payload!");
        }

        Response response = null;

        if (service.invalid(headers, s)) {
            response = Response.status(Response.Status.BAD_REQUEST).entity(s).build();
            log.error("Storage reservation failed");
        }else {
            try {
                service.update(s);
                response = Response.status(Response.Status.NO_CONTENT).build();
                log.info("Storage reservation record updated");
            }catch (Exception e) {
                response = error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        return response;
    }

    @GET
    @Path("/storage")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return the last record of storage resource.", operationId = "get_storage_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Successful, returning existing resources", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Storage getLast(@Context HttpHeaders headers) {
        Storage item = service.findLast();
        return item;
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

    public void setCurrentCoffeeStorage(Double currentCoffeeStorage) {
        this.currentCoffeeStorage = currentCoffeeStorage;
    }

    public void setCurrentMilkStorage(Double currentMilkStorage) {
        this.currentMilkStorage = currentMilkStorage;
    }
}
