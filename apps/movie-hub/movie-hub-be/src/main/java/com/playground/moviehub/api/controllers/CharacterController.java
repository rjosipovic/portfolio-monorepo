package com.playground.moviehub.api.controllers;

import com.playground.moviehub.api.dto.Character;
import com.playground.moviehub.service.characters.CharactersService;
import com.playground.moviehub.utils.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CharacterController {

    private final CharactersService charactersService;

    @GetMapping(ApiPaths.CHARACTERS)
    public Collection<Character> getAll() {
        return charactersService.getAll();
    }

    @GetMapping(ApiPaths.CHARACTERS_WITH_ID)
    public Character getById(@PathVariable UUID id) {
        return charactersService.getById(id);
    }

    @PostMapping(ApiPaths.CHARACTERS)
    public ResponseEntity<Character> create(@RequestBody Character character) {
        var created = charactersService.create(character);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping(ApiPaths.CHARACTERS_WITH_ID)
    public ResponseEntity<Character> update(@PathVariable UUID id, @RequestBody Character character) {
        return ResponseEntity.ok(charactersService.update(id, character));
    }

    @PatchMapping(ApiPaths.CHARACTERS_WITH_ID)
    public ResponseEntity<Character> patch(@PathVariable UUID id, @RequestBody Character character) {
        return ResponseEntity.ok(charactersService.patch(id, character));
    }

    @DeleteMapping(ApiPaths.CHARACTERS_WITH_ID)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        charactersService.delete(id);
        return ResponseEntity.noContent().build();
    }
}