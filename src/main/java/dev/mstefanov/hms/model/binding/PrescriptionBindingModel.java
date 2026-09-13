package dev.mstefanov.hms.model.binding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.INVALID_APPOINTMENT_ID;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.INVALID_NOTES_SIZE;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.INVALID_PATIENT_ID;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.NOTES_CANNOT_BE_NULL;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrescriptionBindingModel {

    @NotNull(message = INVALID_APPOINTMENT_ID)
    private Long appointmentId;

    @NotNull(message = INVALID_PATIENT_ID)
    private Long patientId;

    @NotBlank(message = NOTES_CANNOT_BE_NULL)
    @Size(min = 10, max = 2000, message = INVALID_NOTES_SIZE)
    private String notes;
}
