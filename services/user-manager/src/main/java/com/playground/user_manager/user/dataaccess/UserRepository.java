package com.playground.user_manager.user.dataaccess;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, UUID> {

    Optional<UserEntity> findByAlias(String alias);

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByIdIn(List<UUID> userIds);
}
