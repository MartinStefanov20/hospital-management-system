package dev.mstefanov.hms.model;


import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Appointment extends BaseEntity{

    @NotNull
    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private User doctor;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private User patient;

    private LocalDateTime appointmentTime;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status status;


}
