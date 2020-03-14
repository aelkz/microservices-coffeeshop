package com.redhat.microservices.coffeeshop.product.repository;

import com.redhat.microservices.coffeeshop.product.model.Product;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class ProductRepository extends BaseRepository<Product, UUID> {

    @PostConstruct
    public void init(){
        super.setEntityClass(Product.class);
    }

}
