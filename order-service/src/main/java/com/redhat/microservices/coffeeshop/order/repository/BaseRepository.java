package com.redhat.microservices.coffeeshop.order.repository;

import com.redhat.microservices.coffeeshop.order.model.BaseModel;

import javax.inject.Inject;
import javax.persistence.EntityManager;
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

    public T create(T entity) {
        em.persist(entity);
        em.flush();
        return entity;
    }

    public void update(T entity) {
        em.merge(entity);
    }

    public void delete(ID id) {
        T entity = em.find(entityClass, id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public void delete(T entity) {
        em.remove(entity);
    }

    public T find(ID id) {
        return em.find(entityClass, id);
    }

    public List<T> findAll() {
        TypedQuery<T> query = em.createNamedQuery(entityClass.getSimpleName()+".findAll", entityClass);
        List items = query.getResultList();
        return items;
    }

}
