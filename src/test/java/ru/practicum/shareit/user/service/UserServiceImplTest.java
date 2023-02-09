package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {

    @Test
    void getAllUsers() {
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        UserService userService = new UserServiceImpl(mockUserRepository);
        Mockito
                .when(mockUserRepository.findAll())
                .thenReturn(Collections.emptyList());
        assertEquals(0, userService.getAllUsers().size());
    }

    @Test
    void getById() {
    }

    @Test
    void create() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}