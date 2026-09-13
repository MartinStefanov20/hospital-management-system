package dev.mstefanov.hms.web;

import dev.mstefanov.hms.model.binding.UserRegistrationBindingModel;
import dev.mstefanov.hms.repository.UserRepository;
import dev.mstefanov.hms.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Proves Bean Validation is live on the registration form and that a valid form persists. */
@IntegrationTest
class RegistrationControllerTest {

    private static final String BINDING_RESULT_KEY =
            BindingResult.MODEL_KEY_PREFIX + "userRegistrationBindingModel";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Test
    void passwordMismatchRedirectsBackWithFlag() throws Exception {
        mockMvc.perform(register("mismatch.user", "pass1", "pass2", "Anna", "Smith"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users/register"))
                .andExpect(flash().attributeExists("passwordsMismatch"))
                .andExpect(flash().attribute("userRegistrationBindingModel",
                        org.hamcrest.Matchers.instanceOf(UserRegistrationBindingModel.class)));
        assertThat(userRepository.existsByUsername("mismatch.user")).isFalse();
    }

    @Test
    void tooShortFirstNameProducesBindingError() throws Exception {
        MvcResult result = mockMvc.perform(register("short.name", "pass1", "pass1", "A", "Smith"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users/register"))
                .andExpect(flash().attributeExists("bindingIssues"))
                .andReturn();

        Map<String, ?> flash = result.getFlashMap();
        BindingResult bindingResult = (BindingResult) flash.get(BINDING_RESULT_KEY);
        assertThat(bindingResult).isNotNull();
        assertThat(bindingResult.getFieldError("firstName")).isNotNull();
        assertThat(bindingResult.getFieldError("firstName").getDefaultMessage()).contains("between 2 and 50");
        assertThat(userRepository.existsByUsername("short.name")).isFalse();
    }

    @Test
    void validRegistrationCreatesPatientAndRedirectsToLogin() throws Exception {
        mockMvc.perform(register("valid.user", "pass1", "pass1", "Anna", "Smith"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users/login"));

        assertThat(userRepository.findByUsername("valid.user")).isPresent().get().satisfies(user -> {
            assertThat(user.getPassword()).startsWith("$2");
            assertThat(user.getRoles()).extracting(role -> role.getName()).containsExactly("ROLE_PATIENT");
        });
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder register(
            String username, String password, String confirm, String firstName, String lastName) {
        return post("/users/register")
                .with(csrf())
                .param("username", username)
                .param("password", password)
                .param("confirmPassword", confirm)
                .param("firstName", firstName)
                .param("lastName", lastName)
                .param("salutation", "Mrs.")
                .param("birthday", "1990-01-01");
    }
}
