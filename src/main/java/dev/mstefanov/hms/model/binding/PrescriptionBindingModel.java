package dev.mstefanov.hms.model.binding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrescriptionBindingModel {

    @NotNull(message = INVALID_APPOINTMENT_ID)
    private Long appointmentId;
    @NotNull(message = INVALID_PATIENT_ID)
    private Long patientId;
    @NotNull(message = NOTES_CANNOT_BE_NULL)
    @Size(min = 10, max = 2000, message = INVALID_NOTES_SIZE)
    private String notes;
    @NotNull(message = DOCTOR_USERNAME_MISSING)
    private String doctorUsername;

}
