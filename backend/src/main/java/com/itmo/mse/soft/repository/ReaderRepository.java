package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.Reader;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ReaderRepository extends CrudRepository<Reader, UUID> {
}
