package com.redhat.microservices.coffeeshop.barista.repository;

import com.redhat.microservices.coffeeshop.barista.model.BaseModel;
import java.io.Serializable;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

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
