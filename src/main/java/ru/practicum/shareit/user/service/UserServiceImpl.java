package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public List<User> findAll() {
        log.info("All users sent");
        return repository.findAll();
    }

    @Override
    public User getById(long id) {
        return repository.getById(id).orElseThrow(() -> {
            log.warn("User with id {} not found", id);
            throw new ObjectNotFoundException("User not found");
        });
    }

    @Override
    public User create(User user) {
        checkEmailDuplicates(user.getEmail());
        log.info("User created");
        return repository.create(user);
    }

    @Override
    public User update(long id, UserDto userDto) {
        if (userDto.getEmail() != null) checkEmailDuplicates(userDto.getEmail());
        log.info("User updated");
        return repository.update(id, userDto);
    }

    @Override
    public void delete(long id) {
        log.info("User with id {} deleted", id);
        repository.delete(id);
    }

    private void checkEmailDuplicates(String email) {
        List<User> users = repository.findAll();
        boolean check = users.stream().anyMatch(repoUser -> repoUser.getEmail().equals(email));
        if (check) {
            log.warn("User with same email already exists");
            throw new ValidationException("User with same email already exists");
        }
    }
}
