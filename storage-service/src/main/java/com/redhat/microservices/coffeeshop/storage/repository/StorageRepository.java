package com.redhat.microservices.coffeeshop.storage.repository;

import com.redhat.microservices.coffeeshop.storage.model.Storage;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class StorageRepository extends BaseRepository<Storage, UUID> {

    @PostConstruct
    public void init(){
        super.setEntityClass(Storage.class);
    }

}
