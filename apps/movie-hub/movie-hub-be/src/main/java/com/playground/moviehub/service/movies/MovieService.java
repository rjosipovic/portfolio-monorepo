package com.playground.moviehub.service.movies;

import com.playground.moviehub.api.dto.Movie;

import java.util.Collection;
import java.util.UUID;

public interface MovieService {

    Collection<Movie> getAll();

    Movie getById(UUID id);

    Movie create(Movie movie);

    Movie update(UUID id, Movie movie);

    Movie patch(UUID id, Movie movie);

    void delete(UUID id);
}