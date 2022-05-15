package com.example.proekt.model;

import com.example.proekt.utils.roleValidator.ValidateRole;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import static com.example.proekt.messages.ValidationErrorMessages.*;

@Data
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
