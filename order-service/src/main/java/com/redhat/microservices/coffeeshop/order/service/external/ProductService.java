package com.redhat.microservices.coffeeshop.order.service.external;

import com.redhat.microservices.coffeeshop.order.pojo.Product;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

public class ProductService implements Callable<Product> {
    //private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final String BASE_URI = "http://localhost:8070/api/v1";
    private final Client client = ClientBuilder.newClient();
    private String productId;

    @Override
    @SuppressWarnings("unchecked")
    public Product call() throws Exception {
        WebTarget target = null;
        Response response = null;
        Product product = null;
        JsonObject json = null;

        try {
            target = client.target(BASE_URI);

            response = target
                    .path("/product/{id}")
                    .resolveTemplate("id", getProductId())
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();
            response.bufferEntity();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new RuntimeException("Failed with HTTP error code : " + response.getStatus());
            }
        } finally {
            product = response.readEntity(Product.class);
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
