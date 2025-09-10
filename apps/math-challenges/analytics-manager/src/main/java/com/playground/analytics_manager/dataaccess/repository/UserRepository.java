package com.playground.analytics_manager.dataaccess.repository;

import com.playground.analytics_manager.dataaccess.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CrudRepository<UserEntity, UUID> {
}
