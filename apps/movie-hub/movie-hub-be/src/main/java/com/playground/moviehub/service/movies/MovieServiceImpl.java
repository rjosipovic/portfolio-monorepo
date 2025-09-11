package com.playground.moviehub.service.movies;

import com.playground.moviehub.api.dto.Movie;
import com.playground.moviehub.error.exceptions.ResourceNotFoundException;
import com.playground.moviehub.utils.PatchUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MovieServiceImpl implements MovieService {

    private static final Map<UUID, Movie> MOCKED_MOVIES = Stream.of(
            Movie.builder().id(UUID.randomUUID()).title("Inception").description("A thief who steals corporate secrets through the use of dream-sharing technology.").genre("Sci-Fi").build(),
            Movie.builder().id(UUID.randomUUID()).title("The Shawshank Redemption").description("Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.").genre("Drama").build(),
            Movie.builder().id(UUID.randomUUID()).title("Pulp Fiction").description("The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.").genre("Crime").build(),
            Movie.builder().id(UUID.randomUUID()).title("The Dark Knight").description("When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.").genre("Action").build(),
            Movie.builder().id(UUID.randomUUID()).title("Forrest Gump").description("The presidencies of Kennedy and Johnson, the Vietnam War, the Watergate scandal and other historical events unfold from the perspective of an Alabama man with an IQ of 75, whose only desire is to be reunited with his childhood sweetheart.").genre("Drama").build()
    ).collect(Collectors.toMap(Movie::getId, Function.identity()));

    @Override
    public Collection<Movie> getAll() {
        return MOCKED_MOVIES.values();
    }

    @Override
    public Movie getById(UUID id) {
        return Optional.ofNullable(MOCKED_MOVIES.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Override
    public Movie create(Movie movie) {
        var created = movie.toBuilder()
                .id(UUID.randomUUID())
                .build();
        MOCKED_MOVIES.put(created.getId(), created);
        return created;
    }

    @Override
    public Movie update(UUID id, Movie movie) {
        getById(id);

        var updated = movie.toBuilder()
                .id(id)
                .build();

        MOCKED_MOVIES.put(id, updated);
        return updated;
    }

    @Override
    public Movie patch(UUID id, Movie movie) {
        var existing = getById(id);
        var patched = existing.toBuilder()
                .title(PatchUtils.getOrDefault(movie.getTitle(), existing.getTitle()))
                .description(PatchUtils.getOrDefault(movie.getDescription(), existing.getDescription()))
                .fullDescription(PatchUtils.getOrDefault(movie.getFullDescription(), existing.getFullDescription()))
                .imageUrl(PatchUtils.getOrDefault(movie.getImageUrl(), existing.getImageUrl()))
                .genre(PatchUtils.getOrDefault(movie.getGenre(), existing.getGenre()))
                .build();
        MOCKED_MOVIES.put(id, patched);
        return patched;
    }

    @Override
    public void delete(UUID id) {
        getById(id);
        MOCKED_MOVIES.remove(id);
    }
}