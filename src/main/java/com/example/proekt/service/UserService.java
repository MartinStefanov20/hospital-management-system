package com.example.proekt.service;

import com.example.proekt.model.User;
import com.example.proekt.model.binding.RoleBindingModel;
import com.example.proekt.model.service.UserServiceModel;

import java.util.List;

public interface UserService {

    String getGreeting (String username);

    void registerPatient(UserServiceModel userServiceModel);

    boolean checkIfUsernameExists(String username);

    List<UserServiceModel> getAllDoctors();

    User getUserByUsername(String username);

    String getDoctorAppointmentsGreeting (String username);

    String getUserFullName(Long patientId);

    User getUserById(Long patientId);

    List<String> getAllUsernames();

    List<String> getUserRoles(String username);

    void setUserWithNewRoles(RoleBindingModel roleBindingModel);

    void initUsers();

}
