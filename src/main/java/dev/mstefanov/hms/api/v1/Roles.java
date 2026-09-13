package dev.mstefanov.hms.api.v1;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/** Role helpers for role-aware listings. */
final class Roles {

    static final String ADMIN = "ROLE_ADMIN";
    static final String DOCTOR = "ROLE_DOCTOR";
    static final String PATIENT = "ROLE_PATIENT";

    private Roles() {
    }

    static boolean has(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
