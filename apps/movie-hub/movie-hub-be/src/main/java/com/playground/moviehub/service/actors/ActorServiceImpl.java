package com.playground.moviehub.service.actors;

import com.playground.moviehub.api.dto.Person;
import com.playground.moviehub.error.exceptions.ResourceNotFoundException;
import com.playground.moviehub.utils.PatchUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ActorServiceImpl implements ActorService {

    private static final Map<UUID, Person> MOCKED_ACTORS = Stream.of(
            Person.builder().id(UUID.randomUUID()).name("Tom Hanks").bio("An American actor and filmmaker.").build(),
            Person.builder().id(UUID.randomUUID()).name("Meryl Streep").bio("Often described as the 'best actress of her generation'.").build(),
            Person.builder().id(UUID.randomUUID()).name("Leonardo DiCaprio").bio("An American actor and film producer.").build(),
            Person.builder().id(UUID.randomUUID()).name("Scarlett Johansson").bio("An American actress and singer.").build(),
            Person.builder().id(UUID.randomUUID()).name("Denzel Washington").bio("An American actor, director, and producer.").build(),
            Person.builder().id(UUID.randomUUID()).name("Robert De Niro").bio("An American actor, producer, and director.").build(),
            Person.builder().id(UUID.randomUUID()).name("Morgan Freeman").bio("An American actor, director, and narrator.").build(),
            Person.builder().id(UUID.randomUUID()).name("Cate Blanchett").bio("An Australian actor and theatre director.").build(),
            Person.builder().id(UUID.randomUUID()).name("Brad Pitt").bio("An American actor and film producer.").build(),
            Person.builder().id(UUID.randomUUID()).name("Viola Davis").bio("An American actress and producer.").build()
    ).collect(Collectors.toMap(Person::getId, Function.identity()));

    @Override
    public Collection<Person> getAll() {
        return MOCKED_ACTORS.values();
    }

    @Override
    public Person getById(UUID id) {
        return Optional.ofNullable(MOCKED_ACTORS.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + id));
    }

    @Override
    public Person create(Person actor) {
        var created = actor.toBuilder()
                .id(UUID.randomUUID())
                .build();
        MOCKED_ACTORS.put(created.getId(), created);
        return created;
    }
    
    @Override
    public Person update(UUID id, Person actor) {
        getById(id);

        var updated = actor.toBuilder()
                .id(id)
                .build();
        MOCKED_ACTORS.put(id, updated);
        return updated;
    }

    @Override
    public Person patch(UUID id, Person actor) {
        var existing = getById(id);
        var patched = existing.toBuilder()
                .name(PatchUtils.getOrDefault(actor.getName(), existing.getName()))
                .bio(PatchUtils.getOrDefault(actor.getBio(), existing.getBio()))
                .fullBio(PatchUtils.getOrDefault(actor.getFullBio(), existing.getFullBio()))
                .imageUrl(PatchUtils.getOrDefault(actor.getImageUrl(), existing.getImageUrl()))
                .build();
        MOCKED_ACTORS.put(id, patched);
        return patched;
    }

    @Override
    public void delete(UUID id) {
        getById(id);
        MOCKED_ACTORS.remove(id);
    }
}
