package dev.mstefanov.hms.model;

import dev.mstefanov.hms.utils.roleValidator.ValidateRole;
import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Status extends BaseEntity{


    @NotNull
    @ValidateRole(acceptedValues = {"REQUESTED", "CONFIRMED", "ARCHIVED"}, message = INVALID_STATUS)
    private String name;


    @NotNull
    @Column(name = "status_description", columnDefinition = "TEXT")
    private String statusDescription;

}
