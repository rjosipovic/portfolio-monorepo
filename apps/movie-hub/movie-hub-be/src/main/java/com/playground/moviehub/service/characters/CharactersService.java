package com.playground.moviehub.service.characters;

import com.playground.moviehub.api.dto.Character;

import java.util.Collection;
import java.util.UUID;

public interface CharactersService {

    Collection<Character> getAll();

    Character getById(UUID id);

    Character create(Character character);

    Character update(UUID id, Character character);

    Character patch(UUID id, Character character);

    void delete(UUID id);
}