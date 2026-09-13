package dev.mstefanov.hms.service;

import dev.mstefanov.hms.model.User;
import dev.mstefanov.hms.model.binding.RoleBindingModel;
import dev.mstefanov.hms.model.service.UserServiceModel;

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

}
