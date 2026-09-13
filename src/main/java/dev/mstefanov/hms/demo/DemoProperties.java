package dev.mstefanov.hms.demo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo settings bound from {@code app.demo.*}. The account list is both the seed source for
 * {@link DemoDataService} and what the login page displays when the {@code demo} profile is active.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.demo")
public class DemoProperties {

    /** Shared secret for {@code POST /internal/demo/reset}; the endpoint is disabled when blank. */
    private String resetToken = "";

    private List<Account> accounts = new ArrayList<>();

    @Getter
    @Setter
    public static class Account {
        private String username;
        private String password;
        /** Role without the {@code ROLE_} prefix, e.g. {@code ADMIN}. */
        private String role;
    }
}
