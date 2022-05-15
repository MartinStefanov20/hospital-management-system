package com.example.proekt.model.binding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

import static com.example.proekt.messages.ValidationErrorMessages.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoleBindingModel {

    @NotNull(message = MISSING_USERNAME)
    private String username;
    private boolean patient;
    private boolean doctor;
    private boolean admin;
}
