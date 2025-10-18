package com.playground.user_manager.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.playground.user_manager.user.api.controllers.UserController;
import com.playground.user_manager.user.model.User;
import com.playground.user_manager.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
@AutoConfigureJsonTesters
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private JacksonTester<List<User>> usersJacksonTester;

    @BeforeEach
    void setup() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
    }

    @Test
    void testGetAllUsers() throws Exception {
        //given
        var userId1 = UUID.randomUUID().toString();
        var userId2 = UUID.randomUUID().toString();
        var alias1 = "test-user1";
        var alias2 = "test-user2";
        var user1 = User.builder().id(userId1).alias(alias1).build();
        var user2 = User.builder().id(userId2).alias(alias2).build();
        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));
        //when
        var res = mockMvc.perform(get("/users")).andReturn().getResponse();
        //then
        assertAll(
                () -> assertEquals(200, res.getStatus()),
                () -> assertEquals("application/json", res.getContentType()),
                () -> assertEquals("UTF-8", res.getCharacterEncoding()),
                () -> assertEquals(usersJacksonTester.write(List.of(user1, user2)).getJson(), res.getContentAsString())
        );
    }

    @Test
    void testGetUsersByIds() throws Exception {
        //given
        var userId1 = UUID.randomUUID().toString();
        var userId2 = UUID.randomUUID().toString();
        var alias1 = "test-user1";
        var alias2 = "test-user2";
        var user1 = User.builder().id(userId1).alias(alias1).build();
        var user2 = User.builder().id(userId2).alias(alias2).build();
        when(userService.getUsersByIds(List.of(userId1, userId2))).thenReturn(List.of(user1, user2));
        //when
        var res = mockMvc.perform(get("/users").param("ids", userId1, userId2)).andReturn().getResponse();
        //then
        assertAll(
                () -> assertEquals(200, res.getStatus()),
                () -> assertEquals("application/json", res.getContentType()),
                () -> assertEquals("UTF-8", res.getCharacterEncoding()),
                () -> assertEquals(usersJacksonTester.write(List.of(user1, user2)).getJson(), res.getContentAsString())

        );
    }
}