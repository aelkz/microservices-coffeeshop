package com.redhat.microservices.coffeeshop.order.resource;

import com.redhat.microservices.coffeeshop.order.model.PaymentOrder;
import com.redhat.microservices.coffeeshop.order.model.PaymentOrderItem;
import com.redhat.microservices.coffeeshop.order.pojo.Product;
import com.redhat.microservices.coffeeshop.order.pojo.Storage;
import com.redhat.microservices.coffeeshop.order.service.external.ProductService;
import com.redhat.microservices.coffeeshop.order.service.external.StorageService;
import com.redhat.microservices.coffeeshop.order.service.internal.PaymentOrderService;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Path("/v1")
@org.eclipse.microprofile.openapi.annotations.tags.Tag(name = "Order resource", description = "Order REST resource")
@ApplicationScoped
public class OrderResource {
    private static final Logger log = LoggerFactory.getLogger(OrderResource.class);
    public static final String RESERVATION_ID = "ORDER-RESERVATION";

    @Inject
    private PaymentOrderService service;

    @Resource
    private ManagedExecutorService managedExecutorService;

    @Inject
    private Instance<ProductService> productServiceInstance;

    @Inject
    private Instance<StorageService> storageServiceInstance;

    // Business Metrics (using microprofile)
    @Inject
    @Metric(name = "payment_method_counter", tags = "payment_method=CREDIT_CARD", absolute = true)
    Counter creditCardCounter;

    @Inject
    @Metric(name = "payment_method_counter", tags = "payment_method=MONEY", absolute = true)
    Counter moneyCounter;

    @Inject
    MetricRegistry metricRegistry;

    @POST
    @Path("/order")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a order resource.", operationId = "post_order_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Successful, returning the created resource", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "400", description = "Fail, invalid payload", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    @Counted(unit = MetricUnits.NONE,
            name = "orders",
            absolute = true,
            displayName = "Coffee Shop Orders",
            description = "Metrics to show how many orders with POST http method was fired.",
            tags = {"orders"})
    @Timed(name = "orderProcessingDuration")
    @Metered(name = "orderRequest", tags = {"spec=JAX-RS", "level=REST", "method=POST"})
    public Response order(@Context HttpHeaders headers, PaymentOrder order) throws InterruptedException, ExecutionException {
        if (order == null) {
            return error(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Invalid payload!");
        }

        Response response = null;

        if (service.invalid(headers, order)) {
            response = Response.status(Response.Status.BAD_REQUEST).build();
            log.error("Order creation failed");
        }else {
            try {
                Set<PaymentOrderItem> items = order.getItems();
                double totalRequiredMilk = 0.0;
                double totalRequiredCoffee = 0.0;

                // Retrieve all products from products service REST api in order to compute all required coffee and milk needed.
                for (PaymentOrderItem i : items) {
                    ProductService ps = productServiceInstance.get();
                    ps.setProductId(i.getProductId());

                    Future<Product> futureProduct = managedExecutorService.submit(ps);

                    while (!futureProduct.isDone()) {
                        log.info("Waiting Product API response...");
                        Thread.sleep(50);
                    }

                    Product result = futureProduct.get();
                    totalRequiredMilk += result.getMilk();
                    totalRequiredCoffee += result.getCoffee();
                    log.info("Result is available. Returning Product: " + result.getName() + " with id: " + result.getId());
                    metricRegistry.counter("product_counter", new Tag("type", result.getName())).inc();
                }

                // Check storage for coffee and milk availability
                // STEP 1- create storage OUT record, return storage ID

                Storage storage = new Storage();
                storage.setMilk(totalRequiredMilk);
                storage.setCoffee(totalRequiredCoffee);
                storage.setOp(Storage.Operation.OUT);
                storage.setTransaction(RESERVATION_ID);

                StorageService ss = storageServiceInstance.get();
                ss.setStorage(storage);

                Future<Storage> futureStoragePost = managedExecutorService.submit(ss);

                while (!futureStoragePost.isDone()) {
                    log.info("Waiting Storage API creation response...");
                    Thread.sleep(50);
                }

                // must call get method in order to process
                // get will block until the future is done (blocking)
                storage = futureStoragePost.get(5, TimeUnit.SECONDS);

                // STEP 2- create order, return order ID
                order = service.save(order);

                // STEP 3- update storage OUT record with order ID
                storage.setTransaction(order.getId().toString());
                ss.setStorage(storage);

                ss = storageServiceInstance.get();
                ss.setStorage(storage);

                Future<Storage> futureStoragePut = managedExecutorService.submit(ss);

                while (!futureStoragePut.isDone()) {
                    log.info("Waiting Storage API update response...");
                    Thread.sleep(50);
                }

                // must call get method in order to process
                // get will block until the future is done (blocking)
                futureStoragePut.get(5, TimeUnit.SECONDS);

                response = Response.status(Response.Status.CREATED).entity(order).build();
                log.info("Order added with id:"+order.getId());

                if (order.getPaymentMethod().equals(PaymentOrder.PaymentMethod.CREDIT_CARD)) {
                    creditCardCounter.inc();
                }else {
                    moneyCounter.inc();
                }

                // track total orders in a week (day of week for total coffee orders)
                metricRegistry.counter("order_day_counter", new org.eclipse.microprofile.metrics.Tag("DAY_OF_WEEK", order.getCreation().getDayOfWeek().name().toUpperCase())).inc();

            } catch (InterruptedException e) { // thread was interrupted
                response = error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
                e.printStackTrace();
            } catch (ExecutionException e) { // thread threw an exception
                response = error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
                e.printStackTrace();
            } catch (TimeoutException e) { // timeout before the future task is complete
                response = error(Response.Status.REQUEST_TIMEOUT, e.getMessage());
                e.printStackTrace();
            }
        }

        return response;
    }

    @GET
    @Path("/order")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return a collection of order resource.", operationId = "get_order_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Successful, returning existing resources", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    public PaymentOrder[] getAll(@Context HttpHeaders headers) {
        List<PaymentOrder> items = service.findAll();

        return items.size() > 0 ? items.toArray(new PaymentOrder[0]) : null;
    }

    @POST
    @Path("/order/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(description = "Will return success if execution is ok.", operationId = "post_execute_endpoint")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Successful, returning the created resource", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "400", description = "Fail, invalid payload", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    // TODO
    public Response status(@Suspended AsyncResponse asyncResponse, @Context HttpHeaders headers, PaymentOrder b) {
        Response response = null;
        return response;
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
