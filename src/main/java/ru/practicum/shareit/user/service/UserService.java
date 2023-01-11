package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<User> findAll();

    User getById(long id);

    User create(User user);

    User update(long id, UserDto userDto);

    void delete(long id);
}
