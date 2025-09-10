package com.playground.moviehub.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.moviehub.api.dto.Person;
import com.playground.moviehub.error.exceptions.ResourceNotFoundException;
import com.playground.moviehub.service.actors.ActorService;
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
class ActorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ActorService actorService;

    @InjectMocks
    private ActorController actorController;

    private JacksonTester<Person> actorJson;
    private JacksonTester<List<Person>> actorsJson;
    private Person actor1;
    private Person actor2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(actorController)
                .build();
        JacksonTester.initFields(this, new ObjectMapper());
        actor1 = Person.builder()
                .id(UUID.randomUUID())
                .name("Tom Hanks")
                .bio("An American actor and filmmaker.")
                .build();

        actor2 = Person.builder()
                .id(UUID.randomUUID())
                .name("Scarlett Johansson")
                .bio("An American actress and singer.")
                .build();
    }

    @Test
    void getAllActors_shouldReturnListOfActors() throws Exception {
        //given
        given(actorService.getAll()).willReturn(List.of(actor1, actor2));
        //when & then
        var response = mockMvc.perform(get(ApiPaths.ACTORS))
                .andDo(print())
                .andReturn().getResponse();
        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        var result = actorsJson.parse(response.getContentAsString()).getObject();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(actor1, actor2);
    }

    @Test
    void getActorById_shouldReturnActor() throws Exception {
        //given
        given(actorService.getById(actor1.getId())).willReturn(actor1);

        //when
        var response = mockMvc.perform(get(ApiPaths.ACTORS_WITH_ID, actor1.getId()))
                .andDo(print()) // This will print request and response details
                .andReturn().getResponse();
        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(actorJson.parseObject(response.getContentAsString())).isEqualTo(actor1);
    }

    @Test
    void create_shouldCreateActorAndReturn201Created() throws Exception {
        //given
        given(actorService.create(any(Person.class))).willReturn(actor1);

        //when
        var response = mockMvc.perform(post(ApiPaths.ACTORS) //
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actorJson.write(actor1).getJson())
                        .characterEncoding("UTF-8")) //
                .andDo(print())
                .andReturn().getResponse();
        //then
        assertThat(response.getStatus()).isEqualTo(CREATED.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getHeader("Location")).isEqualTo("http://localhost" + ApiPaths.ACTORS + "/" + actor1.getId());
        assertThat(actorJson.parseObject(response.getContentAsString())).isEqualTo(actor1);
    }

    @Test
    void update_shouldUpdateActorAndReturn200Ok() throws Exception {
        //given
        var actorToUpdate = Person.builder()
                .id(actor1.getId()) // The ID is ignored by the service but good for clarity
                .name("Tom Hanks Updated")
                .bio("An updated bio.")
                .build();

        given(actorService.update(actor1.getId(), actorToUpdate)).willReturn(actorToUpdate);

        //when
        var response = mockMvc.perform(put(ApiPaths.ACTORS_WITH_ID, actor1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actorJson.write(actorToUpdate).getJson())
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(actorJson.parseObject(response.getContentAsString())).isEqualTo(actorToUpdate);
    }

    @Test
    void patch_shouldPartiallyUpdateActorAndReturn200Ok() throws Exception {
        //given
        var patchedInfo = Person.builder().name("Thomas Hanks").build();
        var expectedPatchedActor = actor1.toBuilder().name("Thomas Hanks").build();

        given(actorService.patch(actor1.getId(), patchedInfo)).willReturn(expectedPatchedActor);

        //when
        var response = mockMvc.perform(patch(ApiPaths.ACTORS_WITH_ID, actor1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actorJson.write(patchedInfo).getJson())
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(actorJson.parseObject(response.getContentAsString())).isEqualTo(expectedPatchedActor);
    }

    @Test
    void delete_shouldDeleteActorAndReturn204NoContent() throws Exception {
        //given
        var actorIdToDelete = UUID.randomUUID();
        doNothing().when(actorService).delete(actorIdToDelete);

        //when
        var response = mockMvc.perform(delete(ApiPaths.ACTORS_WITH_ID, actorIdToDelete))
                .andDo(print())
                .andReturn().getResponse();
        //then
        assertThat(response.getStatus()).isEqualTo(NO_CONTENT.value());
    }

    @Test
    void getActorById_whenActorNotFound_shouldReturn404() throws Exception {
        //given
        var nonExistentId = UUID.randomUUID();
        given(actorService.getById(nonExistentId)).willThrow(new ResourceNotFoundException("Actor not found"));

        //when
        var response = mockMvc.perform(get(ApiPaths.ACTORS_WITH_ID, nonExistentId))
                .andDo(print())
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}