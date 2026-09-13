package dev.mstefanov.hms.service.impl;

import dev.mstefanov.hms.exception.NotFoundException;
import dev.mstefanov.hms.model.Role;
import dev.mstefanov.hms.model.User;
import dev.mstefanov.hms.model.service.UserServiceModel;
import dev.mstefanov.hms.repository.UserRepository;
import dev.mstefanov.hms.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RoleService roleService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Spy
    ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    UserServiceImpl service;

    @Test
    void loadUserByUsername_mapsRolesToAuthoritiesAndEnabledFlag() {
        User user = user("dr.house", "ROLE_DOCTOR", "ROLE_ADMIN");
        user.setPassword("$2a$hash");
        when(userRepository.findByUsername("dr.house")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("dr.house");

        assertThat(details.getUsername()).isEqualTo("dr.house");
        assertThat(details.getPassword()).isEqualTo("$2a$hash");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_DOCTOR", "ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_disabledUserIsReportedDisabled() {
        User user = user("locked", "ROLE_PATIENT");
        user.setEnabled(false);
        when(userRepository.findByUsername("locked")).thenReturn(Optional.of(user));

        assertThat(service.loadUserByUsername("locked").isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_unknownUserThrowsUsernameNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void registerPatient_encodesPasswordAndAssignsPatientRole() {
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        UserServiceModel model = new UserServiceModel();
        model.setUsername("newpatient");
        model.setPassword("plain");
        model.setFirstName("New");
        model.setLastName("Patient");
        model.setSalutation("Mrs.");
        model.setBirthday(LocalDate.of(1995, 3, 3));

        service.registerPatient(model);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        User user = saved.getValue();
        assertThat(user.getPassword()).isEqualTo("encoded");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getRegistrationDate()).isNotNull();
        assertThat(user.getRoles()).singleElement().satisfies(role -> {
            assertThat(role.getName()).isEqualTo("ROLE_PATIENT");
            assertThat(role.getUser()).isSameAs(user);
        });
    }

    @Test
    void getUserByUsername_unknownThrowsNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserByUsername("ghost")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllDoctors_excludesAdminsAndPatients() {
        when(userRepository.findAll()).thenReturn(List.of(
                user("doc", "ROLE_DOCTOR"),
                user("chief", "ROLE_DOCTOR", "ROLE_ADMIN"),
                user("pat", "ROLE_PATIENT")));

        assertThat(service.getAllDoctors()).extracting(UserServiceModel::getUsername).containsExactly("doc");
    }

    private static User user(String username, String... roleNames) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("pw");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEnabled(true);
        List<Role> roles = new ArrayList<>();
        for (String name : roleNames) {
            Role role = new Role();
            role.setName(name);
            role.setUser(user);
            roles.add(role);
        }
        user.setRoles(roles);
        return user;
    }
}
