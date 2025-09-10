package com.playground.moviehub.service.characters;

import com.playground.moviehub.api.dto.Character;
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
public class CharacterServiceImpl implements CharactersService {

    // In a real application, these IDs would come from your Movie data.
    // Using fixed UUIDs for demonstration.
    private static final UUID INCEPTION_ID = UUID.fromString("8c7c1c4c-0a36-4cb9-9053-92b253e62c14");
    private static final UUID DARK_KNIGHT_ID = UUID.fromString("5d0f1f3c-7c3b-4f1d-8d5b-2b8b8b2b8b2b");

    private static final Map<UUID, Character> MOCKED_CHARACTERS = Stream.of(
            Character.builder().id(UUID.randomUUID()).name("Darth Vader").description("A powerful Sith Lord.").movieId(UUID.randomUUID()).build(),
            Character.builder().id(UUID.randomUUID()).name("Indiana Jones").description("An adventurous archaeologist.").movieId(UUID.randomUUID()).build(),
            Character.builder().id(UUID.randomUUID()).name("James Bond").description("A British secret agent, code number 007.").movieId(UUID.randomUUID()).build(),
            Character.builder().id(UUID.randomUUID()).name("Ellen Ripley").description("Warrant officer on the Nostromo.").movieId(UUID.randomUUID()).build(),
            Character.builder().id(UUID.randomUUID()).name("The Joker").description("A psychopathic anarchist mastermind.").movieId(DARK_KNIGHT_ID).build(),
            Character.builder().id(UUID.randomUUID()).name("Forrest Gump").description("A man with a low IQ who has witnessed several historical events.").movieId(UUID.randomUUID()).build(),
            Character.builder().id(UUID.randomUUID()).name("Tony Stark").description("Billionaire industrialist and genius inventor.").movieId(UUID.randomUUID()).build(),
            Character.builder().id(UUID.randomUUID()).name("Katniss Everdeen").description("A tribute from District 12.").movieId(UUID.randomUUID()).build(),
            Character.builder().id(UUID.randomUUID()).name("Hermione Granger").description("A brilliant witch from a Muggle family.").movieId(UUID.randomUUID()).build(),
            Character.builder().id(UUID.randomUUID()).name("Vito Corleone").description("The patriarch of the Corleone family.").movieId(UUID.randomUUID()).build()
    ).collect(Collectors.toMap(Character::getId, Function.identity()));

    @Override
    public Collection<Character> getAll() {
        return MOCKED_CHARACTERS.values();
    }

    @Override
    public Character getById(UUID id) {
        return Optional.ofNullable(MOCKED_CHARACTERS.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Character not found with id: " + id));
    }

    @Override
    public Character create(Character character) {
        var created = character.toBuilder()
                .id(UUID.randomUUID())
                .build();
        MOCKED_CHARACTERS.put(created.getId(), created);
        return created;
    }

    @Override
    public Character update(UUID id, Character character) {
        getById(id);
        var updated = character.toBuilder()
                .id(id)
                .build();

        MOCKED_CHARACTERS.put(id, updated);
        return updated;
    }

    @Override
    public Character patch(UUID id, Character character) {
        var existing = getById(id);
        var patched = existing.toBuilder()
                .name(PatchUtils.getOrDefault(character.getName(), existing.getName()))
                .description(PatchUtils.getOrDefault(character.getDescription(), existing.getDescription()))
                .fullDescription(PatchUtils.getOrDefault(character.getFullDescription(), existing.getFullDescription()))
                .imageUrl(PatchUtils.getOrDefault(character.getImageUrl(), existing.getImageUrl()))
                .movieId(PatchUtils.getOrDefault(character.getMovieId(), existing.getMovieId()))
                .build();

        MOCKED_CHARACTERS.put(id, patched);
        return patched;
    }

    @Override
    public void delete(UUID id) {
        getById(id);
        MOCKED_CHARACTERS.remove(id);
    }
}
