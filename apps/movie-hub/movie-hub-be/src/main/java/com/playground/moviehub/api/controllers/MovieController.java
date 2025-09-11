package com.playground.moviehub.api.controllers;

import com.playground.moviehub.api.dto.Movie;
import com.playground.moviehub.service.movies.MovieService;
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
public class MovieController {

    private final MovieService movieService;

    @GetMapping(ApiPaths.MOVIES)
    public Collection<Movie> getAllMovies() {
        return movieService.getAll();
    }

    @GetMapping(ApiPaths.MOVIES_WITH_ID)
    public Movie getMovieById(@PathVariable(name = "id") UUID id) {
        return movieService.getById(id);
    }

    @PostMapping(ApiPaths.MOVIES)
    public ResponseEntity<Movie> create(@RequestBody Movie movie) {
        var created = movieService.create(movie);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping(ApiPaths.MOVIES_WITH_ID)
    public ResponseEntity<Movie> update(@PathVariable(name = "id") UUID id, @RequestBody Movie movie) {
        var updated = movieService.update(id, movie);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping(ApiPaths.MOVIES_WITH_ID)
    public ResponseEntity<Movie> patch(@PathVariable(name = "id") UUID id, @RequestBody Movie movie) {
        var patched = movieService.patch(id, movie);
        return ResponseEntity.ok(patched);
    }

    @DeleteMapping(ApiPaths.MOVIES_WITH_ID)
    public ResponseEntity<Void> delete(@PathVariable(name = "id") UUID id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}