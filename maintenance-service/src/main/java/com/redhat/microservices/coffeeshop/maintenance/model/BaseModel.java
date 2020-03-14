package com.redhat.microservices.coffeeshop.maintenance.model;

import java.io.Serializable;

public interface BaseModel<ID> extends Serializable {

    public <T extends Serializable> ID getId();

}
