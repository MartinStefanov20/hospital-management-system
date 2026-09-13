package dev.mstefanov.hms.model;

import dev.mstefanov.hms.utils.roleValidator.ValidateRole;
import lombok.*;

import jakarta.validation.constraints.NotNull;

import jakarta.persistence.*;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.*;

@Entity

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Role extends BaseEntity{

    @NotNull
    @ValidateRole(acceptedValues={"ROLE_PATIENT", "ROLE_DOCTOR", "ROLE_ADMIN"}, message = INVALID_ROLE)
    private String name;
    @NotNull
    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;
}
