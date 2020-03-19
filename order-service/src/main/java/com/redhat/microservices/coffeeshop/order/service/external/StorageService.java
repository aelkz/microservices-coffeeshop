package com.redhat.microservices.coffeeshop.order.service.external;

import com.redhat.microservices.coffeeshop.order.pojo.Storage;
import com.redhat.microservices.coffeeshop.order.resource.OrderResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

public class StorageService implements Callable<Storage> {
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    private final String BASE_URI = "http://localhost:8270/api/v1";
    private final Client client = ClientBuilder.newClient();
    private Storage storage;

    @Override
    @SuppressWarnings("unchecked")
    public Storage call() throws Exception {
        WebTarget target = null;
        Response response = null;
        Storage storage = null;
        JsonObject json = null;

        try {
            target = client.target(BASE_URI);

            if (!OrderResource.RESERVATION_ID.equals(getStorage().getTransaction())) {
                response = putStorage(target);
            }else {
                response = postStorage(target);
            }

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()
                    && response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                throw new RuntimeException("Failed with HTTP error code : " + response.getStatus());
            }
        } finally {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                storage = response.readEntity(Storage.class);
            }
            response.close();
            client.close();
        }
        return storage;
    }

    private Response postStorage(WebTarget target) {
        Response response;
        response = target
                .path("/storage") // will post storage record (OUT)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(getStorage(), MediaType.APPLICATION_JSON));
        response.bufferEntity();
        return response;
    }

    private Response putStorage(WebTarget target) {
        Response response;
        response = target
                .path("/storage") // will update storage record with transaction id
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(getStorage(), MediaType.APPLICATION_JSON));
        response.bufferEntity();
        return response;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}
