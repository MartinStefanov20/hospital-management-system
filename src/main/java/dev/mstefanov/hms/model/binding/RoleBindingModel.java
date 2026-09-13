package dev.mstefanov.hms.model.binding;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.MISSING_USERNAME;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoleBindingModel {

    @NotBlank(message = MISSING_USERNAME)
    private String username;

    private boolean patient;
    private boolean doctor;
    private boolean admin;
}
