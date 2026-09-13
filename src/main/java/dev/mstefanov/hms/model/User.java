package dev.mstefanov.hms.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "custom_user")
public class User extends BaseEntity{

    @Column(nullable = false, unique = true)
    @Size(min = 4, max = 30, message = INCORRECT_USERNAME_LENGTH)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(name = "first_name", nullable = false)
    @Size(min = 3, max = 20, message = INCORRECT_FIRST_NAME_LENGTH)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    @Size(min = 3, max = 20, message = INCORRECT_LAST_NAME_LENGTH)
    private String lastName;
    private String salutation;
    private LocalDate birthday;
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER
    )
    private List<Role> roles;
    @NotNull
    @PastOrPresent
    private LocalDateTime registrationDate;
    private Double rating;
    private boolean enabled;
}
