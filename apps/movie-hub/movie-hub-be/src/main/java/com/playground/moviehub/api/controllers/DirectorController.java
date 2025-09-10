package com.playground.moviehub.api.controllers;

import com.playground.moviehub.api.dto.Person;
import com.playground.moviehub.service.directors.DirectorService;
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
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping(ApiPaths.DIRECTORS)
    public Collection<Person> getAll() {
        return directorService.getAll();
    }

    @GetMapping(ApiPaths.DIRECTORS_WITH_ID)
    public Person getById(@PathVariable UUID id) {
        return directorService.getById(id);
    }

    @PostMapping(ApiPaths.DIRECTORS)
    public ResponseEntity<Person> create(@RequestBody Person director) {
        var created = directorService.create(director);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping(ApiPaths.DIRECTORS_WITH_ID)
    public ResponseEntity<Person> update(@PathVariable UUID id, @RequestBody Person director) {
        var updated = directorService.update(id, director);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping(ApiPaths.DIRECTORS_WITH_ID)
    public ResponseEntity<Person> patch(@PathVariable UUID id, @RequestBody Person director) {
        var patched = directorService.patch(id, director);
        return ResponseEntity.ok(patched);
    }

    @DeleteMapping(ApiPaths.DIRECTORS_WITH_ID)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        directorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}