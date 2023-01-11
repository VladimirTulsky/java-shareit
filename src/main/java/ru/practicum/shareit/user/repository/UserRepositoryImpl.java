package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {

    Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(long id, UserDto userDto) {
        if (users.containsKey(id)) {
            if (userDto.getName() != null) users.get(id).setName(userDto.getName());
            if (userDto.getEmail() != null) users.get(id).setEmail(userDto.getEmail());
            return users.get(id);
        } else throw new ObjectNotFoundException("User not found");
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }
}
