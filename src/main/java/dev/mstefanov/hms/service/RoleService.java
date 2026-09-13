package dev.mstefanov.hms.service;


import dev.mstefanov.hms.model.Role;

public interface RoleService {

    void deleteCurrentRolesForUser(String username);

    Role getNewRole();

}
