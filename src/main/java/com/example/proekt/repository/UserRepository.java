package com.example.proekt.repository;

import com.example.proekt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    User findOneById (Long id);

    User findOneByUsername (String username);

    User findAllByRoles (String role);

}