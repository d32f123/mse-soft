package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.Reader;
import com.itmo.mse.soft.entity.ReaderLocation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReaderRepository extends JpaRepository<Reader, UUID> {
  Optional<Reader> findByLocation(ReaderLocation location);
}
