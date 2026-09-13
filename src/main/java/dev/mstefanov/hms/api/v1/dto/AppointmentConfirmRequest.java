package dev.mstefanov.hms.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Doctor's confirmation of a requested appointment")
public record AppointmentConfirmRequest(
        @Schema(description = "ISO-8601 local date-time, must be in the future", example = "2026-10-01T10:30:00")
        @NotNull(message = "appointmentTime is required")
        @Future(message = "appointmentTime must be in the future")
        LocalDateTime appointmentTime) {
}
