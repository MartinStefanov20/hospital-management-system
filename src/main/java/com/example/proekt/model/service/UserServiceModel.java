package com.example.proekt.model.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserServiceModel extends BaseServiceModel{

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String salutation;
    private LocalDate birthday;
    private List<RoleServiceModel> roles;
    private LocalDateTime registrationDate;
    private Double rating;
    private boolean enabled;

}
