package com.redhat.microservices.coffeeshop.barista.repository;

import com.redhat.microservices.coffeeshop.barista.model.Barista;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class BaristaRepository extends BaseRepository<Barista, UUID> {

    @PostConstruct
    public void init(){
        super.setEntityClass(Barista.class);
    }

}
