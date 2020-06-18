package com.itmo.mse.soft.service;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public abstract class EntityService<T> {

    protected abstract CrudRepository<T, UUID> getEntityRepository();

    public Iterable<T> getAll() {
        return getEntityRepository().findAll();
    }

    public Optional<T> getById(UUID id) {
        return getEntityRepository().findById(id);
    }

    public T save(T t) {
        return getEntityRepository().save(t);
    }

    public Iterable<T> saveAll(Iterable<T> t) {
        return getEntityRepository().saveAll(t);
    }

}
