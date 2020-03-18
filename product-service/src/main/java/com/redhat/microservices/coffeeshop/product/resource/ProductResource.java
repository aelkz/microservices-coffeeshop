package com.redhat.microservices.coffeeshop.product.resource;

import com.redhat.microservices.coffeeshop.product.model.Product;
import com.redhat.microservices.coffeeshop.product.service.ProductService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
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
@Tag(name = "Product resource", description = "Product REST resource")
@ApplicationScoped
public class ProductResource {
    //private static final Logger log = LoggerFactory.getLogger(ProductResource.class);

    @Inject
    ProductService service;

    @POST
    @Path("/product")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a product resource.", operationId = "post_product_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Successful, returning the created resource", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "400", description = "Fail, invalid payload", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Response add(@Context HttpHeaders headers, Product b) {

        if (b == null) {
            return error(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Invalid payload!");
        }

        Response response = null;

        if (service.invalid(headers, b)) {
            response = Response.status(Response.Status.BAD_REQUEST).entity(b).build();
            //log.error("Product creation failed");
        }else {
            try {
                b = service.save(b);
                response = Response.status(Response.Status.CREATED).entity(b).build();
                //log.info("Product:"+b.getName()+" added with id:"+b.getId());
            }catch (Exception e) {
                response = error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        return response;
    }

    @GET
    @Path("/product")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a collection of product resource.", operationId = "get_product_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Successful, returning existing resources", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Response getAll(@Context HttpHeaders headers) {
        List<Product> items = service.findAll();
        return Response.status(Response.Status.OK).entity(items).build();
    }

    @GET
    @Path("/product/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a single product resource.", operationId = "get_single_product_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Successful, returning existing resource", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public Response getOne(@Context HttpHeaders headers, @PathParam("id") String productId) {
        Product item = service.findOne(productId);

        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }else {
            return Response.status(Response.Status.OK).entity(item).build();
        }
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
