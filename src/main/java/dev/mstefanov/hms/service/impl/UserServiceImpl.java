package dev.mstefanov.hms.service.impl;

import dev.mstefanov.hms.model.Role;
import dev.mstefanov.hms.model.User;
import dev.mstefanov.hms.model.binding.RoleBindingModel;
import dev.mstefanov.hms.model.service.RoleServiceModel;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserDetailsService, UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }


    private UserDetails map(User userEntity) {

        return new org.springframework.security.core.userdetails.User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.
                        getRoles().
                        stream().
                        map(this::map).
                        collect(Collectors.toList())
        );
    }

    private GrantedAuthority map(Role role) {
        return new SimpleGrantedAuthority(role.getName());
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> userEntityOpt = userRepository.findByUsername(username);

        return userEntityOpt.
                map(this::map).
                orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found!"));
    }

    @Override
    public String getGreeting(String username) {

        User user = this.userRepository.findByUsername(username).orElse(null);
        String greeting = "";

        if (user != null) {
            greeting = "Welcome " + user.getSalutation() + " " + user.getLastName();
        }

        return greeting;
    }

    @Override
    public void registerPatient(UserServiceModel userServiceModel) {

        User user = this.modelMapper.map(userServiceModel, User.class);
        user.setPassword(this.passwordEncoder.encode(userServiceModel.getPassword()));
        Role rolePatient = new Role();
        rolePatient.setName("ROLE_PATIENT");
        rolePatient.setUser(user);
        user.setRoles(List.of(rolePatient));
        user.setRegistrationDate(LocalDateTime.now());
        user.setEnabled(true);

        this.userRepository.save(user);
    }

    @Override
    public boolean checkIfUsernameExists(String username) {

        return this.userRepository.findByUsername(username).orElse(null) != null;

    }

    @Override
    public List<UserServiceModel> getAllDoctors() {

        List<UserServiceModel> users = new ArrayList<>();

        for (User user : this.userRepository.findAll()) {
            for (Role role : user.getRoles()) {
                if (role.getName().equals("ROLE_DOCTOR")) {
                    users.add(this.modelMapper.map(user, UserServiceModel.class));
                    break;
                }
            }
        }


        for (int i = 0; i < users.size(); i++) {
            for (RoleServiceModel role : users.get(i).getRoles()) {
                if (role.getName().equals("ROLE_ADMIN")) {
                    users.remove(i);
                    i--;
                    break;
                }
            }

        }

        return users;
    }

    @Override
    public User getUserByUsername(String username) {

        return this.userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public String getDoctorAppointmentsGreeting(String username) {

        User user = this.userRepository.findByUsername(username).orElse(null);
        String greeting = "";

        if (user != null) {
            greeting = user.getSalutation() + " " + user.getLastName() + "`s Appointments";
        }

        return greeting;
    }

    @Override
    public String getUserFullName(Long patientId) {

        User user = this.userRepository.findOneById(patientId);

        return user.getSalutation() + " " + user.getFirstName() + " " + user.getLastName();
    }

    @Override
    public User getUserById(Long patientId) {

        return this.userRepository.findOneById(patientId);
    }

    @Override
    public List<String> getAllUsernames() {

        return this.userRepository.findAll().stream().map(User::getUsername).collect(Collectors.toList());
    }

    @Override
    public List<String> getUserRoles(String username) {

        UserServiceModel user = this.modelMapper.map(this.userRepository.findOneByUsername(username), UserServiceModel.class);
        List<String> roles = new ArrayList<>();

        for (RoleServiceModel role : user.getRoles()) {
            roles.add(role.getName());
        }

        return roles;
    }

    @Transactional
    @Override
    public void setUserWithNewRoles(RoleBindingModel roleBindingModel) {

        User user = this.userRepository.findOneByUsername(roleBindingModel.getUsername());
        user.setRoles(new ArrayList<>());

        this.userRepository.save(user);
        List<Role> roles = new ArrayList<>();

        if (roleBindingModel.isAdmin()) {
            Role role = this.roleService.getNewRole();
            role.setName("ROLE_ADMIN");
            role.setUser(user);
            roles.add(role);
        }
        if (roleBindingModel.isPatient()) {
            Role role = this.roleService.getNewRole();
            role.setName("ROLE_PATIENT");
            role.setUser(user);
            roles.add(role);
        }
        if (roleBindingModel.isDoctor()) {
            Role role = this.roleService.getNewRole();
            role.setName("ROLE_DOCTOR");
            role.setUser(user);
            roles.add(role);
        }

        user.setRoles(roles);
        this.userRepository.save(user);
    }

}
