package com.redhat.microservices.coffeeshop.order.repository.impl;

import com.redhat.microservices.coffeeshop.order.model.PaymentOrderItem;
import com.redhat.microservices.coffeeshop.order.repository.BaseRepository;
import com.redhat.microservices.coffeeshop.order.repository.OrderItemCustomRepository;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class OrderItemRepository extends BaseRepository<PaymentOrderItem, UUID> implements OrderItemCustomRepository {

    @Inject
    private EntityManager em;

    @PostConstruct
    public void init(){
        super.setEntityClass(PaymentOrderItem.class);
    }

    @Override
    public List<PaymentOrderItem> findItemsByOrderId(UUID paymentOrderId) {
        TypedQuery<PaymentOrderItem> query = em.createNamedQuery(PaymentOrderItem.QUERY_FIND_ALL_BY_ORDER, PaymentOrderItem.class);
        query.setParameter("paymentOrderId", paymentOrderId);

        List items = query.getResultList();
        return items;
    }
}
