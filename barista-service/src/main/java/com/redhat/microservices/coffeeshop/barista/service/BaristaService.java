package com.redhat.microservices.coffeeshop.barista.service;

import com.redhat.microservices.coffeeshop.barista.model.Barista;
import com.redhat.microservices.coffeeshop.barista.repository.BaristaRepository;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.HttpHeaders;
import java.util.List;

public class BaristaService {

    @Inject
    private BaristaRepository repository;

    @Transactional(Transactional.TxType.REQUIRED)
    public Barista save(Barista e) {
        e = repository.create(e);
        return e;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<Barista> findAll() {
        return repository.findAll();
    }

    public Boolean invalid(HttpHeaders headers, Barista b) {
        // MultivaluedMap<String, String> map = headers.getRequestHeaders();
        // return map.containsKey("blah");
        return b.getEmail() == null || b.getName() == null;
    }

}
