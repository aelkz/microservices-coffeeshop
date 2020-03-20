package com.redhat.microservices.coffeeshop.order.service.external;

import com.redhat.microservices.coffeeshop.order.exception.ProductNotFoundException;
import com.redhat.microservices.coffeeshop.order.pojo.Product;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

public class ProductService implements Callable<Product> {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final String BASE_URI = "http://localhost:8070/api/v1";
    private final Client client = ClientBuilder.newClient();
    private String productId;

    @Inject
    @ConfigProperty(name = "coffeeshop.routes.product-service")
    String uri;

    @Override
    @SuppressWarnings("unchecked")
    public Product call() throws Exception {
        WebTarget target = null;
        Response response = null;
        Product product = null;
        JsonObject json = null;

        try {
            target = client.target((uri != null && !"".equals(uri)) ? uri : BASE_URI);

            log.info("calling product endpoint at: ".concat(uri));

            response = target
                    .path("/product/{id}")
                    .resolveTemplate("id", getProductId())
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();
            response.bufferEntity();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new ProductNotFoundException(getProductId(), response.getStatus());
            }else {
                product = response.readEntity(Product.class);
            }
        } finally {
            response.close();
            client.close();
        }
        return product;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
