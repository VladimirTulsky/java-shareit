package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select new ru.practicum.shareit.user.model.User(us.id, us.name, us.email)" +
            "from User as us " +
            "group by us.id " +
            "order by us.id asc")
    List<User> findAllUsers();
}