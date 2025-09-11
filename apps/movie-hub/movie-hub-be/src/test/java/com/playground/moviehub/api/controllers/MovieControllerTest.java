package com.playground.moviehub.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.moviehub.api.dto.Movie;
import com.playground.moviehub.error.exceptions.ResourceNotFoundException;
import com.playground.moviehub.service.movies.MovieService;
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
class MovieControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private JacksonTester<Movie> movieJson;
    private JacksonTester<List<Movie>> moviesJson;

    private Movie movie1;
    private Movie movie2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(movieController)
                .build();
        JacksonTester.initFields(this, new ObjectMapper());

        movie1 = Movie.builder()
                .id(UUID.randomUUID())
                .title("Inception")
                .description("A thief who steals corporate secrets through the use of dream-sharing technology.")
                .build();

        movie2 = Movie.builder()
                .id(UUID.randomUUID())
                .title("The Dark Knight")
                .description("When the menace known as the Joker wreaks havoc...")
                .build();
    }

    @Test
    void getAll_shouldReturnListOfMovies() throws Exception {
        given(movieService.getAll()).willReturn(List.of(movie1, movie2));

        var response = mockMvc.perform(get(ApiPaths.MOVIES))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        var result = moviesJson.parse(response.getContentAsString()).getObject();
        assertThat(result).containsExactlyInAnyOrder(movie1, movie2);
    }

    @Test
    void getById_shouldReturnMovie() throws Exception {
        given(movieService.getById(movie1.getId())).willReturn(movie1);

        var response = mockMvc.perform(get(ApiPaths.MOVIES_WITH_ID, movie1.getId()))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(movieJson.parseObject(response.getContentAsString())).isEqualTo(movie1);
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        var nonExistentId = UUID.randomUUID();
        given(movieService.getById(nonExistentId)).willThrow(new ResourceNotFoundException("Movie not found"));

        var response = mockMvc.perform(get(ApiPaths.MOVIES_WITH_ID, nonExistentId))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void create_shouldCreateMovieAndReturn201Created() throws Exception {
        given(movieService.create(any(Movie.class))).willReturn(movie1);
        var response = mockMvc.perform(post(ApiPaths.MOVIES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson.write(movie1).getJson())
                        .characterEncoding("UTF-8"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(CREATED.value());
        assertThat(response.getHeader("Location")).isEqualTo("http://localhost" + ApiPaths.MOVIES + "/" + movie1.getId());
        assertThat(movieJson.parseObject(response.getContentAsString())).isEqualTo(movie1);
    }

    @Test
    void update_shouldUpdateMovieAndReturn200Ok() throws Exception {
        //given
        var movieToUpdate = Movie.builder()
                .title("Inception: The Director's Cut")
                .description("An updated description.")
                .build();

        given(movieService.update(movie1.getId(), movieToUpdate)).willReturn(movieToUpdate);

        //when
        var response = mockMvc.perform(put(ApiPaths.MOVIES_WITH_ID, movie1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson.write(movieToUpdate).getJson())
                        .characterEncoding("UTF-8"))
                .andDo(print()).andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(movieJson.parseObject(response.getContentAsString())).isEqualTo(movieToUpdate);
    }

    @Test
    void patch_shouldPartiallyUpdateMovieAndReturn200Ok() throws Exception {
        //given
        var patchedInfo = Movie.builder().genre("Sci-Fi Thriller").build();
        var expectedPatchedMovie = movie1.toBuilder().genre("Sci-Fi Thriller").build();

        given(movieService.patch(movie1.getId(), patchedInfo)).willReturn(expectedPatchedMovie);

        //when
        var response = mockMvc.perform(patch(ApiPaths.MOVIES_WITH_ID, movie1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson.write(patchedInfo).getJson())
                        .characterEncoding("UTF-8"))
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThat(movieJson.parseObject(response.getContentAsString())).isEqualTo(expectedPatchedMovie);
    }

    @Test
    void delete_shouldDeleteMovieAndReturn204NoContent() throws Exception {
        var movieIdToDelete = UUID.randomUUID();
        doNothing().when(movieService).delete(movieIdToDelete);

        var response = mockMvc.perform(delete(ApiPaths.MOVIES_WITH_ID, movieIdToDelete))
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(NO_CONTENT.value());
    }
}