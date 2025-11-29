package com.playground.moviehub.dataaccess.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("ACTOR")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActorEntity extends PersonEntity {

    public static ActorEntity create(String name, String bio, String fullBio, String imageUrl) {
        return PersonEntity.create(new ActorEntity(), name, bio, fullBio, imageUrl);
    }
}