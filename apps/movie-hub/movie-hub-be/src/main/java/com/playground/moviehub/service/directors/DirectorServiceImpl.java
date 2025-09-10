package com.playground.moviehub.service.directors;

import com.playground.moviehub.api.dto.Person;
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
public class DirectorServiceImpl implements DirectorService {

    private static final Map<UUID, Person> MOCKED_DIRECTORS = Stream.of(
            Person.builder().id(UUID.randomUUID()).name("Christopher Nolan").bio("A British-American film director, producer, and screenwriter.").build(),
            Person.builder().id(UUID.randomUUID()).name("Quentin Tarantino").bio("An American film director, writer, and actor.").build(),
            Person.builder().id(UUID.randomUUID()).name("Martin Scorsese").bio("An American film director, producer, screenwriter, and actor.").build(),
            Person.builder().id(UUID.randomUUID()).name("Steven Spielberg").bio("An American film director, producer, and screenwriter.").build(),
            Person.builder().id(UUID.randomUUID()).name("Greta Gerwig").bio("An American actress, writer, and director.").build()
    ).collect(Collectors.toMap(Person::getId, Function.identity()));

    @Override
    public Collection<Person> getAll() {
        return MOCKED_DIRECTORS.values();
    }

    @Override
    public Person getById(UUID id) {
        return Optional.ofNullable(MOCKED_DIRECTORS.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with id: " + id));
    }

    @Override
    public Person create(Person director) {
        var created = director.toBuilder()
                .id(UUID.randomUUID())
                .build();
        MOCKED_DIRECTORS.put(created.getId(), created);
        return created;
    }

    @Override
    public Person update(UUID id, Person director) {
        getById(id);

        var updated = director.toBuilder()
                .id(id)
                .build();

        MOCKED_DIRECTORS.put(id, updated);
        return updated;
    }

    @Override
    public Person patch(UUID id, Person director) {
        var existing = getById(id);
        var patched = existing.toBuilder()
                .name(PatchUtils.getOrDefault(director.getName(), existing.getName()))
                .bio(PatchUtils.getOrDefault(director.getBio(), existing.getBio()))
                .fullBio(PatchUtils.getOrDefault(director.getFullBio(), existing.getFullBio()))
                .imageUrl(PatchUtils.getOrDefault(director.getImageUrl(), existing.getImageUrl()))
                .build();
        MOCKED_DIRECTORS.put(id, patched);
        return patched;
    }

    @Override
    public void delete(UUID id) {
        getById(id);
        MOCKED_DIRECTORS.remove(id);
    }
}