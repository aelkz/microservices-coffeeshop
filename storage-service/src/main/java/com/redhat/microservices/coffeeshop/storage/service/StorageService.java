package com.redhat.microservices.coffeeshop.storage.service;

import com.redhat.microservices.coffeeshop.storage.model.Storage;
import com.redhat.microservices.coffeeshop.storage.repository.StorageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.HttpHeaders;
import java.time.LocalDateTime;
import java.util.List;

public class StorageService {
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    @Inject
    private StorageRepository repository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Storage save(Storage s) throws RuntimeException {

        Storage last = findLast();
        boolean firstRecord = false;

        if (last == null) {
            last = new Storage();
            last.setTotalMilk(0.0);
            last.setTotalCoffee(0.0);
            last.setMilk(0.0);
            last.setCoffee(0.0);
            firstRecord = true;
        }

        if (s.getOp() == Storage.Operation.IN) {
            s.setTotalCoffee(last.getTotalCoffee() + s.getCoffee());
            s.setTotalMilk(last.getTotalMilk() + s.getMilk());
        }else {
            if (firstRecord) {
                throw new RuntimeException("Storage is empty");
            }else {
                if (s.getCoffee() > last.getTotalCoffee()) {
                    throw new RuntimeException("Coffee storage is empty");
                }else if (s.getMilk() > last.getTotalMilk()) {
                    throw new RuntimeException("Milk storage is empty");
                }else if (s.getTransaction() == null || s.getTransaction().length() == 0) {
                    throw new RuntimeException("Transaction ID cannot be null");
                }

                s.setTotalCoffee(last.getTotalCoffee() - s.getCoffee());
                s.setTotalMilk(last.getTotalMilk() - s.getMilk());
            }
        }

        s.setCreation(LocalDateTime.now());
        s = repository.create(s);

        return s;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void update(Storage s) {
        s.setCreation(LocalDateTime.now());
        repository.update(s);
    }

    @Transactional(value = Transactional.TxType.REQUIRED)
    public List<Storage> findAll() {
        return repository.findAll();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Storage findLast() {
        return repository.findLast();
    }

    public Boolean invalid(HttpHeaders headers, Storage s) {
        // MultivaluedMap<String, String> map = headers.getRequestHeaders();
        // return map.containsKey("blah");
        return s.getCoffee() == null || s.getMilk() == null;
    }

}
