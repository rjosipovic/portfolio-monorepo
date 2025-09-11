package com.playground.moviehub.service.actors;

import com.playground.moviehub.api.dto.Person;

import java.util.Collection;
import java.util.UUID;

public interface ActorService {

    Collection<Person> getAll();

    Person getById(UUID id);

    Person create(Person actor);

    Person update(UUID id, Person actor);

    Person patch(UUID id, Person actor);

    void delete(UUID id);
}
