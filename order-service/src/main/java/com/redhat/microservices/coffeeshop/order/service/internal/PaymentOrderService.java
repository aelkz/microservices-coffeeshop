package com.redhat.microservices.coffeeshop.order.service.internal;

import com.redhat.microservices.coffeeshop.order.model.PaymentOrder;
import com.redhat.microservices.coffeeshop.order.repository.impl.PaymentOrderRepository;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.HttpHeaders;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class PaymentOrderService {

    @Inject
    private PaymentOrderRepository repository;

    @Transactional(Transactional.TxType.REQUIRED)
    public PaymentOrder save(PaymentOrder o) {
        o.setCreation(LocalDateTime.now());

        o = repository.create(o);
        return o;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<PaymentOrder> findAll() {
        return repository.findAll();
    }

    public Boolean invalid(HttpHeaders headers, PaymentOrder o) {
        // MultivaluedMap<String, String> map = headers.getRequestHeaders();
        // return map.containsKey("blah");
        return o.getPaymentMethod() == null || o.getItems() == null;
    }

}
