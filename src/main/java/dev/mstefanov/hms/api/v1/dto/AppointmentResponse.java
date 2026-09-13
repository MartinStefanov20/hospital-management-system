package dev.mstefanov.hms.api.v1.dto;

import dev.mstefanov.hms.model.service.AppointmentServiceModel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "An appointment between a patient and a doctor")
public record AppointmentResponse(
        @Schema(example = "17") Long id,
        UserSummary doctor,
        UserSummary patient,
        @Schema(description = "Null until the doctor confirms", example = "2026-10-01T10:30:00", nullable = true)
        LocalDateTime appointmentTime,
        @Schema(allowableValues = {"REQUESTED", "CONFIRMED", "ARCHIVED"}, example = "REQUESTED") String status) {

    public static AppointmentResponse from(AppointmentServiceModel appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                UserSummary.from(appointment.getDoctor()),
                UserSummary.from(appointment.getPatient()),
                appointment.getAppointmentTime(),
                appointment.getStatus() == null ? null : appointment.getStatus().getName());
    }
}
