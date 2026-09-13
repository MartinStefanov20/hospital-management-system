package dev.mstefanov.hms.configurations;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 6 configuration. Authentication is wired automatically from the single
 * {@link org.springframework.security.core.userdetails.UserDetailsService} bean
 * ({@code UserServiceImpl}) and the {@code PasswordEncoder} bean declared in
 * {@link ApplicationBeanConfiguration}.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String DEMO_RESET_ENDPOINT = "/internal/demo/reset";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {

        http
                .sessionManagement(session -> session
                        .maximumSessions(100)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/users/login")
                        .sessionRegistry(sessionRegistry))
                .csrf(csrf -> csrf.ignoringRequestMatchers(DEMO_RESET_ENDPOINT))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers(
                                "/", "/index", "/about-us",
                                "/users/login", "/users/login-error", "/users/register",
                                "/access-denied", "/error",
                                "/assets/**",
                                "/actuator/health", "/actuator/health/**",
                                DEMO_RESET_ENDPOINT).permitAll()
                        .requestMatchers("/admin/**", "/users/admin/**").hasRole("ADMIN")
                        .requestMatchers("/doctor/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/home").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                        .requestMatchers(
                                "/appointments/**", "/appointment/**",
                                "/prescriptions/**",
                                "/departments/**", "/department/**",
                                "/doctors/**").hasAnyRole("PATIENT", "ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .successForwardUrl("/home")
                        .failureForwardUrl("/users/login-error"))
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }
}
