package dev.mstefanov.hms.service.impl;

import dev.mstefanov.hms.exception.NotFoundException;
import dev.mstefanov.hms.model.Role;
import dev.mstefanov.hms.model.User;
import dev.mstefanov.hms.model.binding.RoleBindingModel;
import dev.mstefanov.hms.model.service.UserServiceModel;
import dev.mstefanov.hms.repository.UserRepository;
import dev.mstefanov.hms.service.RoleService;
import dev.mstefanov.hms.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserDetailsService, UserService {

    static final String ROLE_ADMIN = "ROLE_ADMIN";
    static final String ROLE_DOCTOR = "ROLE_DOCTOR";
    static final String ROLE_PATIENT = "ROLE_PATIENT";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService, ModelMapper modelMapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found!"));
    }

    private UserDetails toUserDetails(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName()))
                .toList();
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }

    @Override
    public String getGreeting(String username) {
        User user = getUserByUsername(username);
        return "Welcome " + user.getSalutation() + " " + user.getLastName();
    }

    @Override
    @Transactional
    public void registerPatient(UserServiceModel userServiceModel) {
        User user = modelMapper.map(userServiceModel, User.class);
        user.setPassword(passwordEncoder.encode(userServiceModel.getPassword()));
        Role rolePatient = new Role();
        rolePatient.setName(ROLE_PATIENT);
        rolePatient.setUser(user);
        user.setRoles(new ArrayList<>(List.of(rolePatient)));
        user.setRegistrationDate(LocalDateTime.now());
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public boolean checkIfUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /** Users holding ROLE_DOCTOR but not ROLE_ADMIN (admins never appear as bookable doctors). */
    @Override
    public List<UserServiceModel> getAllDoctors() {
        return userRepository.findAll().stream()
                .filter(user -> hasRole(user, ROLE_DOCTOR) && !hasRole(user, ROLE_ADMIN))
                .map(user -> modelMapper.map(user, UserServiceModel.class))
                .toList();
    }

    private static boolean hasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(role -> roleName.equals(role.getName()));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User '" + username + "' not found"));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));
    }

    @Override
    public String getDoctorAppointmentsGreeting(String username) {
        User user = getUserByUsername(username);
        return user.getSalutation() + " " + user.getLastName() + "`s Appointments";
    }

    @Override
    public String getUserFullName(Long patientId) {
        User user = getUserById(patientId);
        return user.getSalutation() + " " + user.getFirstName() + " " + user.getLastName();
    }

    @Override
    public List<String> getAllUsernames() {
        return userRepository.findAll().stream().map(User::getUsername).toList();
    }

    @Override
    public List<String> getUserRoles(String username) {
        return getUserByUsername(username).getRoles().stream().map(Role::getName).toList();
    }

    @Transactional
    @Override
    public void setUserWithNewRoles(RoleBindingModel roleBindingModel) {
        User user = getUserByUsername(roleBindingModel.getUsername());
        user.setRoles(new ArrayList<>());
        userRepository.save(user);

        List<Role> roles = new ArrayList<>();
        if (roleBindingModel.isAdmin()) {
            roles.add(newRole(ROLE_ADMIN, user));
        }
        if (roleBindingModel.isPatient()) {
            roles.add(newRole(ROLE_PATIENT, user));
        }
        if (roleBindingModel.isDoctor()) {
            roles.add(newRole(ROLE_DOCTOR, user));
        }
        user.setRoles(roles);
        userRepository.save(user);
    }

    private Role newRole(String name, User user) {
        Role role = roleService.getNewRole();
        role.setName(name);
        role.setUser(user);
        return role;
    }
}
