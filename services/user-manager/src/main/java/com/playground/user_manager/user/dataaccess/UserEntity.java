package com.playground.user_manager.user.dataaccess;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "users")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "alias", unique = true)
    private String alias;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "gender")
    private String gender;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(this.id) ? Objects.hashCode(id) : getClass().hashCode();
    }

    public static UserEntity create(String alias, String email, LocalDate birthdate, String gender) {
        if (Objects.isNull(alias)) {
            throw new IllegalArgumentException("UserEntity alias must not be null");
        }
        if (Objects.isNull(email)) {
            throw new IllegalArgumentException("UserEntity email must not be null");
        }
        var userEntity = new UserEntity();
        userEntity.setAlias(alias);
        userEntity.setEmail(email);
        userEntity.setBirthdate(birthdate);
        userEntity.setGender(gender);
        return userEntity;
    }

}
