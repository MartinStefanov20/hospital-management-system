package com.example.proekt.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;


    @Autowired
    public SecurityConfig(@Qualifier("userServiceImpl") UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder, SessionRegistry sessionRegistry) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.sessionRegistry = sessionRegistry;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.userDetailsService(this.userDetailsService).passwordEncoder(
                this.passwordEncoder);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/h2/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.sessionManagement()
                .maximumSessions(100)               //(1)
                .maxSessionsPreventsLogin(false)    //(2)
                .expiredUrl("/users/login")          //(3)
                .sessionRegistry(this.sessionRegistry);

        http.authorizeRequests()
                .antMatchers("/users/login", "/", "/users/login-error", "/resources/**", "/index").permitAll()
                .antMatchers("/home").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                .antMatchers("/doctors/**").hasAnyRole("PATIENT", "ADMIN")
                .antMatchers("/departments/**").hasAnyRole("PATIENT", "ADMIN")
                .antMatchers("/department/**").hasAnyRole("PATIENT", "ADMIN")
                .antMatchers("/prescriptions/**").hasAnyRole("PATIENT", "ADMIN")
                .antMatchers("/appointments/**").hasAnyRole("PATIENT", "ADMIN")
                .antMatchers("/appointment/**").hasAnyRole("PATIENT", "ADMIN")
                .antMatchers("/doctors/**").hasAnyRole("PATIENT", "ADMIN")
                .antMatchers("/about-us").hasAnyRole("PATIENT", "ADMIN")
                .antMatchers("/doctor/**").hasAnyRole("DOCTOR", "ADMIN")
                .antMatchers("/admin/**").hasAnyRole("ADMIN")
                .and()
                .exceptionHandling().accessDeniedPage("/access-denied")
                .and()
                .formLogin()
                .loginPage("/users/login")
                .successForwardUrl("/home")
                .failureForwardUrl("/users/login-error")
                .and()
                .logout()
                .logoutUrl("/users/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");

    }
}

