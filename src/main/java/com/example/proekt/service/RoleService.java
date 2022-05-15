package com.example.proekt.service;


import com.example.proekt.model.Role;

public interface RoleService {

    void deleteCurrentRolesForUser(String username);

    Role getNewRole();

}
