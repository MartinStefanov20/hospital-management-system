package dev.mstefanov.hms.model.binding;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.INCORRECT_BIRTHDAY;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.INCORRECT_FIRST_NAME_LENGTH;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.INCORRECT_LAST_NAME_LENGTH;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.INCORRECT_PASSWORD_LENGTH;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.INCORRECT_SALUTATION;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.INCORRECT_USERNAME_LENGTH;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.MISSING_BIRTHDAY;

/**
 * Registration form. The name constraints intentionally match {@link dev.mstefanov.hms.model.User}
 * so that a form that passes validation can always be persisted.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRegistrationBindingModel {

    @NotNull(message = INCORRECT_USERNAME_LENGTH)
    @Size(min = 4, max = 30, message = INCORRECT_USERNAME_LENGTH)
    private String username;

    @NotNull(message = INCORRECT_PASSWORD_LENGTH)
    @Size(min = 4, max = 16, message = INCORRECT_PASSWORD_LENGTH)
    private String password;

    @NotNull(message = INCORRECT_PASSWORD_LENGTH)
    @Size(min = 4, max = 16, message = INCORRECT_PASSWORD_LENGTH)
    private String confirmPassword;

    @NotNull(message = INCORRECT_FIRST_NAME_LENGTH)
    @Size(min = 2, max = 50, message = INCORRECT_FIRST_NAME_LENGTH)
    private String firstName;

    @NotNull(message = INCORRECT_LAST_NAME_LENGTH)
    @Size(min = 2, max = 50, message = INCORRECT_LAST_NAME_LENGTH)
    private String lastName;

    @NotNull(message = INCORRECT_SALUTATION)
    @Size(min = 2, message = INCORRECT_SALUTATION)
    private String salutation;

    @NotNull(message = MISSING_BIRTHDAY)
    @Past(message = INCORRECT_BIRTHDAY)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
