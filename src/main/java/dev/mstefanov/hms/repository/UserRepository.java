package dev.mstefanov.hms.repository;

import dev.mstefanov.hms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    User findOneById (Long id);

    User findOneByUsername (String username);

}