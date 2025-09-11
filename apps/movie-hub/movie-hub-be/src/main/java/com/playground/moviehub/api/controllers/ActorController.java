package com.playground.moviehub.api.controllers;

import com.playground.moviehub.api.dto.Person;
import com.playground.moviehub.service.actors.ActorService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ActorController {

    private final ActorService actorService;

    @GetMapping(ApiPaths.ACTORS)
    public Collection<Person> getAllActors() {
        return actorService.getAll();
    }

    @GetMapping(ApiPaths.ACTORS_WITH_ID)
    public Person getActorById(@PathVariable(name = "id") UUID id) {
        return actorService.getById(id);
    }
    
    @PostMapping(ApiPaths.ACTORS)
    public ResponseEntity<Person> create(@RequestBody Person actor) {
        var created = actorService.create(actor);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping(ApiPaths.ACTORS_WITH_ID)
    public ResponseEntity<Person> update(@PathVariable(name = "id") UUID id, @RequestBody Person actor) {
        var updated = actorService.update(id, actor);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping(ApiPaths.ACTORS_WITH_ID)
    public ResponseEntity<Person> patch(@PathVariable(name = "id") UUID id, @RequestBody Person actor) {
        var patched = actorService.patch(id, actor);
        return ResponseEntity.ok(patched);
    }

    @DeleteMapping(ApiPaths.ACTORS_WITH_ID)
    public ResponseEntity<Void> delete(@PathVariable(name = "id") UUID id) {
        actorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
