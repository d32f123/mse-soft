package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.Body;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface BodyRepository extends CrudRepository<Body, UUID> {
}
