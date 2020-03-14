package com.redhat.microservices.coffeeshop.order.repository.impl;

import com.redhat.microservices.coffeeshop.order.model.PaymentOrder;
import com.redhat.microservices.coffeeshop.order.repository.BaseRepository;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class PaymentOrderRepository extends BaseRepository<PaymentOrder, UUID> {

    @PostConstruct
    public void init(){
        super.setEntityClass(PaymentOrder.class);
    }

}
