package com.playground.moviehub.dataaccess.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("DIRECTOR")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectorEntity extends PersonEntity {

    public static DirectorEntity create(String name, String bio, String fullBio, String imageUrl) {
        return PersonEntity.create(new DirectorEntity(), name, bio, fullBio, imageUrl);
    }
}