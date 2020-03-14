package com.redhat.microservices.coffeeshop.storage.repository;

import com.redhat.microservices.coffeeshop.storage.model.BaseModel;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

@Transactional
public abstract class BaseRepository<T extends BaseModel, ID> implements Serializable {

    @Inject
    private EntityManager em;

    private Class<T> entityClass;

    abstract public void init();

    public BaseRepository() { }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public T create(T entity) {
        em.persist(entity);
        em.flush();
        return entity;
    }

    public void update(T entity) {
        em.merge(entity);
    }

    public T find(ID id) {
        return em.find(entityClass, id);
    }

    public List<T> findAll() {
        TypedQuery<T> query = em.createNamedQuery(entityClass.getSimpleName()+".findAll", entityClass);
        List<T> items = query.getResultList();
        return items;
    }

    public T findLast() {
        TypedQuery<T> query = em.createNamedQuery(entityClass.getSimpleName()+".findLast", entityClass);
        query.setMaxResults(1);

        try {
            return query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

}
