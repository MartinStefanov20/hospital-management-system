package dev.mstefanov.hms.configurations;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security 6 configuration with two filter chains:
 * <ol>
 *   <li>{@code /api/**}: stateless HTTP Basic, no CSRF, 401 with {@code WWW-Authenticate} for anonymous calls.
 *       Fine-grained role checks live on the controllers via {@code @PreAuthorize}.</li>
 *   <li>everything else: session-based form login for the Thymeleaf UI.</li>
 * </ol>
 * Authentication is wired automatically from the single {@code UserDetailsService} bean ({@code UserServiceImpl})
 * and the {@code PasswordEncoder} bean declared in {@link ApplicationBeanConfiguration}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String DEMO_RESET_ENDPOINT = "/internal/demo/reset";

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        // URL-level denials (none configured today) answer 403 without an HTML redirect
                        .defaultAccessDeniedHandlerFor(
                                (request, response, denied) -> response.sendError(HttpStatus.FORBIDDEN.value()),
                                AntPathRequestMatcher.antMatcher("/api/**")));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
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
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
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
                .exceptionHandling(ex -> ex.accessDeniedHandler(webAccessDeniedHandler()))
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

    /**
     * A stale or missing CSRF token almost always means the login form sat open until the session
     * expired (or the demo instance restarted). Send those users back to the login page with a hint
     * instead of a bare 403; real authorization failures still get the access-denied page.
     */
    private AccessDeniedHandler webAccessDeniedHandler() {
        AccessDeniedHandlerImpl forbidden = new AccessDeniedHandlerImpl();
        forbidden.setErrorPage("/access-denied");
        return (request, response, denied) -> {
            if (denied instanceof CsrfException) {
                response.sendRedirect(request.getContextPath() + "/users/login?expired");
                return;
            }
            forbidden.handle(request, response, denied);
        };
    }
}
