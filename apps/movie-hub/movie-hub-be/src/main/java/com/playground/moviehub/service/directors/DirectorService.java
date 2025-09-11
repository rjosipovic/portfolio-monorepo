package com.playground.moviehub.service.directors;

import com.playground.moviehub.api.dto.Person;

import java.util.Collection;
import java.util.UUID;

public interface DirectorService {

    Collection<Person> getAll();

    Person getById(UUID id);

    Person create(Person director);

    Person update(UUID id, Person director);

    Person patch(UUID id, Person director);

    void delete(UUID id);
}