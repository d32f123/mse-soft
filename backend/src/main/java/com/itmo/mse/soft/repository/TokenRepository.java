package com.itmo.mse.soft.repository;

import com.itmo.mse.soft.entity.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TokenRepository extends CrudRepository<Token, UUID> {
}
