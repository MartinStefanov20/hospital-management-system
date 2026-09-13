package dev.mstefanov.hms.model.binding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.INVALID_NOTES_SIZE;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.NOTES_CANNOT_BE_NULL;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.PRESCRIPTION_ID_MANDATORY;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrescriptionEditBindingModel {

    @NotNull(message = PRESCRIPTION_ID_MANDATORY)
    private Long prescriptionId;

    @NotBlank(message = NOTES_CANNOT_BE_NULL)
    @Size(min = 10, max = 2000, message = INVALID_NOTES_SIZE)
    private String notes;
}
