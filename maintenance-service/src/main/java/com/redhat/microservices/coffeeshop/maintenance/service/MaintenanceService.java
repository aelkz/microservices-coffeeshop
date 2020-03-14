package com.redhat.microservices.coffeeshop.maintenance.service;

import com.redhat.microservices.coffeeshop.maintenance.model.Maintenance;
import com.redhat.microservices.coffeeshop.maintenance.repository.MaintenanceRepository;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.HttpHeaders;
import java.time.LocalDateTime;
import java.util.List;

public class MaintenanceService {

    @Inject
    private MaintenanceRepository repository;

    @Transactional(Transactional.TxType.REQUIRED)
    public Maintenance save(Maintenance m) {
        m.setCreation(LocalDateTime.now());
        m = repository.create(m);
        return m;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<Maintenance> findAll() {
        return repository.findAll();
    }

    public Boolean invalid(HttpHeaders headers, Maintenance m) {
        // MultivaluedMap<String, String> map = headers.getRequestHeaders();
        // return map.containsKey("blah");
        return m.getBarista()== null && m.getBarista().length() == 36;
    }

}
