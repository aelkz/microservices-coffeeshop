package com.redhat.microservices.coffeeshop.order.repository;

import com.redhat.microservices.coffeeshop.order.model.PaymentOrderItem;
import java.util.List;
import java.util.UUID;

public interface OrderItemCustomRepository {

    public List<PaymentOrderItem> findItemsByOrderId(UUID orderId);

}
