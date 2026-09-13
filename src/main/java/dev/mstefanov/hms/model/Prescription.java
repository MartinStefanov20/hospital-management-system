package dev.mstefanov.hms.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Prescription extends BaseEntity{

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private User prescribedBy;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private User prescribeTo;
    @NotNull
    @FutureOrPresent(message = INCORRECT_PRESCRIPTION_DATE)
    private LocalDate date;
    @NotNull
    @Size(min = 3, max = 2000, message = INVALID_PRESCRIPTION_MESSAGE)
    private String prescriptionNotes;
    @ManyToOne
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    private Appointment appointment;

}
