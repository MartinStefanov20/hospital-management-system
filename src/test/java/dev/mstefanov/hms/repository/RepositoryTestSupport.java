package dev.mstefanov.hms.repository;

import dev.mstefanov.hms.model.Role;
import dev.mstefanov.hms.model.Status;
import dev.mstefanov.hms.model.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Builders for the minimal entity graph the repository tests need. Statuses come from Flyway V2. */
final class RepositoryTestSupport {

    private RepositoryTestSupport() {
    }

    static User user(TestEntityManager em, String username, String roleName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("{noop}secret");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setSalutation("Mr.");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setRegistrationDate(LocalDateTime.now());
        user.setEnabled(true);
        Role role = new Role();
        role.setName(roleName);
        role.setUser(user);
        user.setRoles(new ArrayList<>(List.of(role)));
        return em.persistAndFlush(user);
    }

    static Status status(TestEntityManager em, String name) {
        return em.getEntityManager()
                .createQuery("select s from Status s where s.name = :name", Status.class)
                .setParameter("name", name)
                .getSingleResult();
    }
}
