package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.Pigsty;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PigstyRepository extends CrudRepository<Pigsty, UUID> {
}
