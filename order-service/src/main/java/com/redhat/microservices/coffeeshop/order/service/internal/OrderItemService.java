package com.redhat.microservices.coffeeshop.order.service.internal;

import com.redhat.microservices.coffeeshop.order.model.PaymentOrderItem;
import com.redhat.microservices.coffeeshop.order.repository.impl.OrderItemRepository;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.HttpHeaders;
import java.util.List;

@Transactional
public class OrderItemService {

    @Inject
    private OrderItemRepository repository;

    @Transactional(Transactional.TxType.REQUIRED)
    public PaymentOrderItem save(PaymentOrderItem o) {
        o = repository.create(o);
        return o;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<PaymentOrderItem> findAll() {
        return repository.findAll();
    }

    public Boolean invalid(HttpHeaders headers, PaymentOrderItem oi) {
        // MultivaluedMap<String, String> map = headers.getRequestHeaders();
        // return map.containsKey("blah");
        return oi.getSize() == null || oi.getProductId() == null;
    }

}
