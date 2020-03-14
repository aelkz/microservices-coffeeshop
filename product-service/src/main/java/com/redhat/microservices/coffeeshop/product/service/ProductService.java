package com.redhat.microservices.coffeeshop.product.service;

import com.redhat.microservices.coffeeshop.product.model.Product;
import com.redhat.microservices.coffeeshop.product.repository.ProductRepository;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.UUID;

public class ProductService {

    @Inject
    private ProductRepository repository;

    @Transactional(Transactional.TxType.REQUIRED)
    public Product save(Product p) {
        p = repository.create(p);
        return p;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Product findOne(String productId) {
        UUID uuid = UUID.fromString(productId);
        return repository.find(uuid);
    }

    public Boolean invalid(HttpHeaders headers, Product p) {
        // MultivaluedMap<String, String> map = headers.getRequestHeaders();
        // return map.containsKey("blah");
        return p.getCoffee() == null | p.getName() == null;
    }

}
