package com.redhat.microservices.coffeeshop.order.exception;

public class ProductNotFoundException extends RuntimeException {

    private String id;
    private Integer status;

    public ProductNotFoundException(String id, Integer status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
