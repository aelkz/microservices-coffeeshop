package com.redhat.microservices.coffeeshop.order.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // Don't enable CORS for secured resources. This is made automatically already by the adapter
        if (!requestContext.getUriInfo().getPath().endsWith("-secured")) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        }
    }

}
