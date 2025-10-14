package com.playground.analytics_manager.dataaccess.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.Objects;
import java.util.UUID;

@Node("User")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @NotNull
    private UUID id;

    @Version
    private Long version;

    @NotBlank
    @Property("alias")
    private String alias;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity that)) return false;
        if (this.id == null || that.id == null) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }

    public static UserEntity create(UUID id, String alias) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("UserEntity id must not be null");
        }
        if (Objects.isNull(alias)) {
            throw new IllegalArgumentException("UserEntity alias must not be null");
        }
        var userEntity = new UserEntity();
        userEntity.setId(id);
        userEntity.setAlias(alias);
        return userEntity;
    }
}
