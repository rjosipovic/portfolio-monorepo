package com.playground.moviehub.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.moviehub.api.dto.Person;
import com.playground.moviehub.error.exceptions.ResourceNotFoundException;
import com.playground.moviehub.service.directors.DirectorService;
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
class DirectorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DirectorService directorService;

    @InjectMocks
    private DirectorController directorController;

    private JacksonTester<Person> directorJson;
    private JacksonTester<List<Person>> directorsJson;

    private Person director1;
    private Person director2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(directorController)
                .build();
        JacksonTester.initFields(this, new ObjectMapper());

        director1 = Person.builder()
                .id(UUID.randomUUID())
                .name("Christopher Nolan")
                .bio("A British-American film director, producer, and screenwriter.")
                .build();

        director2 = Person.builder()
                .id(UUID.randomUUID())
                .name("Greta Gerwig")
                .bio("An American actress, writer, and director.")
                .build();
    }

    @Test
    void getAll_shouldReturnListOfDirectors() throws Exception {
        given(directorService.getAll()).willReturn(List.of(director1, director2));

        var response = mockMvc.perform(get(ApiPaths.DIRECTORS))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        var result = directorsJson.parse(response.getContentAsString()).getObject();
        assertThat(result).containsExactlyInAnyOrder(director1, director2);
    }

    @Test
    void getById_shouldReturnDirector() throws Exception {
        given(directorService.getById(director1.getId())).willReturn(director1);

        var response = mockMvc.perform(get(ApiPaths.DIRECTORS_WITH_ID, director1.getId()))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(directorJson.parseObject(response.getContentAsString())).isEqualTo(director1);
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        var nonExistentId = UUID.randomUUID();
        given(directorService.getById(nonExistentId)).willThrow(new ResourceNotFoundException("Director not found"));

        var response = mockMvc.perform(get(ApiPaths.DIRECTORS_WITH_ID, nonExistentId))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void create_shouldCreateDirectorAndReturn201Created() throws Exception {
        given(directorService.create(any(Person.class))).willReturn(director1);

        var response = mockMvc.perform(post(ApiPaths.DIRECTORS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(directorJson.write(director1).getJson())
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(CREATED.value());
        assertThat(response.getHeader("Location")).isEqualTo("http://localhost" + ApiPaths.DIRECTORS + "/" + director1.getId());
        assertThat(directorJson.parseObject(response.getContentAsString())).isEqualTo(director1);
    }

    @Test
    void update_shouldUpdateDirectorAndReturn200Ok() throws Exception {
        //given
        var directorToUpdate = Person.builder()
                .name("C. Nolan (Updated)")
                .bio("An updated bio for the director.")
                .build();

        given(directorService.update(director1.getId(), directorToUpdate)).willReturn(directorToUpdate);

        //when
        var response = mockMvc.perform(put(ApiPaths.DIRECTORS_WITH_ID, director1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(directorJson.write(directorToUpdate).getJson())
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(directorJson.parseObject(response.getContentAsString())).isEqualTo(directorToUpdate);
    }

    @Test
    void patch_shouldPartiallyUpdateDirectorAndReturn200Ok() throws Exception {
        //given
        var patchedInfo = Person.builder().bio("A visionary director.").build();
        var expectedPatchedDirector = director1.toBuilder().bio("A visionary director.").build();

        given(directorService.patch(director1.getId(), patchedInfo)).willReturn(expectedPatchedDirector);

        //when
        var response = mockMvc.perform(patch(ApiPaths.DIRECTORS_WITH_ID, director1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(directorJson.write(patchedInfo).getJson())
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(directorJson.parseObject(response.getContentAsString())).isEqualTo(expectedPatchedDirector);
    }

    @Test
    void delete_shouldDeleteDirectorAndReturn204NoContent() throws Exception {
        var directorIdToDelete = UUID.randomUUID();
        doNothing().when(directorService).delete(directorIdToDelete);

        var response = mockMvc.perform(delete(ApiPaths.DIRECTORS_WITH_ID, directorIdToDelete))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(NO_CONTENT.value());
    }
}