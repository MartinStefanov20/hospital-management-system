package dev.mstefanov.hms.web;

import dev.mstefanov.hms.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Authorization rules of both filter chains against the real security configuration and seeded demo users. */
@IntegrationTest
class SecurityRulesTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/users/login"));
    }

    @Test
    void patientIsDeniedAdminRoutes() throws Exception {
        mockMvc.perform(get("/admin/manage-roles-select-user").with(user("patient").roles("PATIENT")))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    void adminCanOpenAdminRoutes() throws Exception {
        mockMvc.perform(get("/admin/manage-roles-select-user").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void staticAssetsArePublic() throws Exception {
        mockMvc.perform(get("/assets/css/style.css"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith("text/css")));
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void swaggerAndApiDocsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Hospital Management System API"));
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/swagger-ui/index.html")));
    }

    @Test
    void apiWithoutCredentialsIs401WithBasicChallenge() throws Exception {
        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Basic")));
    }

    @Test
    void apiWithDemoPatientCredentialsIs200() throws Exception {
        mockMvc.perform(get("/api/v1/departments").with(httpBasic("patient", "patient123")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void apiWithWrongPasswordIs401() throws Exception {
        mockMvc.perform(get("/api/v1/departments").with(httpBasic("patient", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loggedInUserGetsHtml404Page() throws Exception {
        mockMvc.perform(get("/department").param("name", "Nope").with(user("patient").roles("PATIENT")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("Page not found")));
    }
}
