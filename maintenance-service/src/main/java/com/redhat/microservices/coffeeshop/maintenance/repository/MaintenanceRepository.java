package com.redhat.microservices.coffeeshop.maintenance.repository;

import com.redhat.microservices.coffeeshop.maintenance.model.Maintenance;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class MaintenanceRepository extends BaseRepository<Maintenance, UUID> {

    @PostConstruct
    public void init(){
        super.setEntityClass(Maintenance.class);
    }

}
