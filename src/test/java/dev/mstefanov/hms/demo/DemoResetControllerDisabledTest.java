package dev.mstefanov.hms.demo;

import dev.mstefanov.hms.configurations.ApplicationBeanConfiguration;
import dev.mstefanov.hms.configurations.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Without a configured token the reset endpoint must not be reachable at all. */
@WebMvcTest(DemoResetController.class)
@Import({SecurityConfig.class, ApplicationBeanConfiguration.class})
@EnableConfigurationProperties(DemoProperties.class)
@TestPropertySource(properties = "app.demo.reset-token=")
class DemoResetControllerDisabledTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DemoDataService demoDataService;

    @MockitoBean
    UserDetailsService userDetailsService;

    @Test
    void endpointIsHiddenWhenTokenIsBlank() throws Exception {
        mockMvc.perform(post("/internal/demo/reset").header("X-Reset-Token", "anything"))
                .andExpect(status().isNotFound());
        verify(demoDataService, never()).reset();
    }
}
