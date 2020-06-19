package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.Pigsty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PigstyRepository extends JpaRepository<Pigsty, UUID> {
}
