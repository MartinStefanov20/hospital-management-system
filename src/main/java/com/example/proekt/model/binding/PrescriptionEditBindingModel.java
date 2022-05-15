package com.example.proekt.model.binding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static com.example.proekt.messages.ValidationErrorMessages.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrescriptionEditBindingModel {

    @NotNull(message = PRESCRIPTION_ID_MANDATORY)
    private long prescriptionId;
    @NotNull(message = NOTES_CANNOT_BE_NULL)
    @Size(min = 10, max = 2000, message = INVALID_NOTES_SIZE)
    private String notes;

}
