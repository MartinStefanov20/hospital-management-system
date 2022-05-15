package com.example.proekt.service.impl;

import com.example.proekt.model.Role;
import com.example.proekt.model.User;
import com.example.proekt.model.binding.RoleBindingModel;
import com.example.proekt.model.service.RoleServiceModel;
import com.example.proekt.model.service.UserServiceModel;
import com.example.proekt.repository.UserRepository;
import com.example.proekt.service.RoleService;
import com.example.proekt.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Override
    public void initUsers() {
        if (this.userRepository.count() == 0) {
            User patient = new User();
            patient.setUsername("martin.stefanov");
            patient.setPassword(passwordEncoder.encode("patient"));
            patient.setEnabled(true);
            patient.setFirstName("Martin");
            patient.setLastName("Stefanov");
            patient.setEnabled(true);
            patient.setRegistrationDate(LocalDateTime.now());
            patient.setBirthday(LocalDate.of(1991, 04, 05));
            patient.setSalutation("Mr.");

            Role rolePatient = this.roleService.getNewRole();
            rolePatient.setName("ROLE_PATIENT");
            rolePatient.setUser(patient);

            patient.setRoles(List.of(rolePatient));

//            userRepository.save(patient);


            User doctor = new User();
            doctor.setUsername("martin.doctor");
            doctor.setPassword(passwordEncoder.encode("doctor"));
            doctor.setEnabled(true);
            doctor.setFirstName("Martin");
            doctor.setLastName("Doctor");
            doctor.setEnabled(true);
            doctor.setRegistrationDate(LocalDateTime.now());
            doctor.setBirthday(LocalDate.of(1985, 11, 02));
            doctor.setSalutation("Dr.");

            Role roleDoctor = this.roleService.getNewRole();
            roleDoctor.setName("ROLE_DOCTOR");
            roleDoctor.setUser(doctor);
            doctor.setRoles(List.of(roleDoctor));

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEnabled(true);
            admin.setEnabled(true);
            admin.setFirstName("Admin");
            admin.setLastName("Adminov");
            admin.setEnabled(true);
            admin.setRegistrationDate(LocalDateTime.now());
            admin.setBirthday(LocalDate.of(681, 01, 01));
            admin.setSalutation("Master");

            Role roleAdminDoctor = this.roleService.getNewRole();
            roleAdminDoctor.setName("ROLE_DOCTOR");
            roleAdminDoctor.setUser(admin);

            Role roleAdminPatient = this.roleService.getNewRole();
            roleAdminPatient.setName("ROLE_PATIENT");
            roleAdminPatient.setUser(admin);

            Role roleAdmin = this.roleService.getNewRole();
            roleAdmin.setName("ROLE_ADMIN");
            roleAdmin.setUser(admin);

            admin.setRoles(List.of(roleAdminPatient, roleAdminDoctor, roleAdmin));

            userRepository.saveAll(List.of(patient, doctor, admin));
        }
    }
}