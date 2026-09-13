package dev.mstefanov.hms.model.binding;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static dev.mstefanov.hms.messages.ValidationErrorMessages.APPOINTMENT_DATE_IN_PAST;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.APPOINTMENT_DATE_MANDATORY;
import static dev.mstefanov.hms.messages.ValidationErrorMessages.MISSING_APPOINTMENT_ID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentConfirmationBindingModel {

    @NotNull(message = MISSING_APPOINTMENT_ID)
    private Long id;

    /** Bound from an {@code <input type="datetime-local">}, i.e. ISO local date-time such as {@code 2026-01-31T10:30}. */
    @NotNull(message = APPOINTMENT_DATE_MANDATORY)
    @Future(message = APPOINTMENT_DATE_IN_PAST)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateAndTime;
}
