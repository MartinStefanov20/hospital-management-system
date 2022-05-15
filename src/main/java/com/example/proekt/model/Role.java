package com.example.proekt.model;

import com.example.proekt.utils.roleValidator.ValidateRole;
import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;

import static com.example.proekt.messages.ValidationErrorMessages.*;

@Data
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
