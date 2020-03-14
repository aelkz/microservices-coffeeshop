package com.redhat.microservices.coffeeshop.order.model;

import java.io.Serializable;

public interface BaseModel<ID> extends Serializable {

    public <T extends Serializable> ID getId();

}
