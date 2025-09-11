package com.playground.moviehub.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.moviehub.api.dto.Character;
import com.playground.moviehub.error.exceptions.ResourceNotFoundException;
import com.playground.moviehub.service.characters.CharactersService;
import com.playground.moviehub.utils.ApiPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
class CharacterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CharactersService charactersService;

    @InjectMocks
    private CharacterController characterController;

    private JacksonTester<Character> characterJson;
    private JacksonTester<List<Character>> charactersJson;

    private Character character1;
    private Character character2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(characterController)
                .build();
        JacksonTester.initFields(this, new ObjectMapper());

        character1 = Character.builder()
                .id(UUID.randomUUID())
                .name("The Joker")
                .description("A psychopathic anarchist mastermind.")
                .build();

        character2 = Character.builder()
                .id(UUID.randomUUID())
                .name("Ellen Ripley")
                .description("Warrant officer on the Nostromo.")
                .build();
    }

    @Test
    void getAll_shouldReturnListOfCharacters() throws Exception {
        given(charactersService.getAll()).willReturn(List.of(character1, character2));

        var response = mockMvc.perform(get(ApiPaths.CHARACTERS))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        var result = charactersJson.parse(response.getContentAsString()).getObject();
        assertThat(result).containsExactlyInAnyOrder(character1, character2);
    }

    @Test
    void getById_shouldReturnCharacter() throws Exception {
        given(charactersService.getById(character1.getId())).willReturn(character1);

        var response = mockMvc.perform(get(ApiPaths.CHARACTERS_WITH_ID, character1.getId()))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(characterJson.parseObject(response.getContentAsString())).isEqualTo(character1);
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        var nonExistentId = UUID.randomUUID();
        given(charactersService.getById(nonExistentId)).willThrow(new ResourceNotFoundException("Character not found"));

        var response = mockMvc.perform(get(ApiPaths.CHARACTERS_WITH_ID, nonExistentId))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void create_shouldCreateCharacterAndReturn201Created() throws Exception {
        given(charactersService.create(any(Character.class))).willReturn(character1);

        var response = mockMvc.perform(post(ApiPaths.CHARACTERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(characterJson.write(character1).getJson())
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(CREATED.value());
        assertThat(response.getHeader("Location")).isEqualTo("http://localhost" + ApiPaths.CHARACTERS + "/" + character1.getId());
        assertThat(characterJson.parseObject(response.getContentAsString())).isEqualTo(character1);
    }

    @Test
    void update_shouldUpdateCharacterAndReturn200Ok() throws Exception {
        //given
        var characterToUpdate = Character.builder()
                .name("The Joker (Updated)")
                .description("An updated description.")
                .build();

        given(charactersService.update(character1.getId(), characterToUpdate)).willReturn(characterToUpdate);

        //when
        var response = mockMvc.perform(put(ApiPaths.CHARACTERS_WITH_ID, character1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(characterJson.write(characterToUpdate).getJson())
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(characterJson.parseObject(response.getContentAsString())).isEqualTo(characterToUpdate);
    }

    @Test
    void patch_shouldPartiallyUpdateCharacterAndReturn200Ok() throws Exception {
        //given
        var patchedInfo = Character.builder().description("A true mastermind.").build();
        var expectedPatchedCharacter = character1.toBuilder().description("A true mastermind.").build();

        given(charactersService.patch(character1.getId(), patchedInfo)).willReturn(expectedPatchedCharacter);

        //when
        var response = mockMvc.perform(patch(ApiPaths.CHARACTERS_WITH_ID, character1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(characterJson.write(patchedInfo).getJson())
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(characterJson.parseObject(response.getContentAsString())).isEqualTo(expectedPatchedCharacter);
    }

    @Test
    void delete_shouldDeleteCharacterAndReturn204NoContent() throws Exception {
        var characterIdToDelete = UUID.randomUUID();
        doNothing().when(charactersService).delete(characterIdToDelete);

        var response = mockMvc.perform(delete(ApiPaths.CHARACTERS_WITH_ID, characterIdToDelete))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(NO_CONTENT.value());
    }
}