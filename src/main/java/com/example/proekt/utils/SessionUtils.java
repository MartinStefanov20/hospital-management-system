package com.example.proekt.utils;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SessionUtils {

    private final SessionRegistry sessionRegistry;

    public SessionUtils(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }


    public void expireUserSessions(String username) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {

            UserDetails userDetails = (UserDetails) principal;
            if (userDetails.getUsername().equals(username)) {
                for (SessionInformation information : sessionRegistry.getAllSessions(userDetails, true)) {
                    information.expireNow();
                }
            }

        }
    }
}