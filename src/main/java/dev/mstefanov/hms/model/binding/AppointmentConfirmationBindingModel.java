package dev.mstefanov.hms.model.binding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentConfirmationBindingModel {

    private String id;
    @NotNull(message = APPOINTMENT_DATE_MANDATORY)
    private String dateAndTime;

}