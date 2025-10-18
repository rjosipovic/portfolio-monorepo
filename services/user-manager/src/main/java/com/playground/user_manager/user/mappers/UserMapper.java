package com.playground.user_manager.user.mappers;

import com.playground.user_manager.auth.api.dto.RegisterUserRequest;
import com.playground.user_manager.auth.api.dto.RegisteredUser;
import com.playground.user_manager.user.dataaccess.UserEntity;
import com.playground.user_manager.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toModel(UserEntity entity);

    @Mapping(source = "id", target = "userId")
    RegisteredUser toRegisteredUserDto(UserEntity entity);

    UserEntity toEntity(RegisterUserRequest dto);

    /**
     * This method is a custom object factory for creating UserEntity instances.
     * MapStruct will use this method instead of a default constructor whenever it needs to
     * create an entity from a RegisterUserRequest DTO.
     * This allows us to enforce the use of the static factory method on the entity.
     */
    @ObjectFactory
    default UserEntity createEntity(RegisterUserRequest dto) {
        return UserEntity.create(
                dto.getAlias(),
                dto.getEmail(),
                dto.getBirthdate(),
                dto.getGender()
        );
    }
}
