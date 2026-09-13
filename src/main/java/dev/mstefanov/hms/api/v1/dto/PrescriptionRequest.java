package dev.mstefanov.hms.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Doctor issues a prescription; when appointmentId is given that appointment is archived")
public record PrescriptionRequest(
        @Schema(example = "4")
        @NotNull(message = "patientId is required")
        Long patientId,

        @Schema(description = "Optional appointment this prescription belongs to", example = "17", nullable = true)
        Long appointmentId,

        @Schema(example = "Ibuprofen 400 mg every 8 hours for 5 days with food.")
        @NotBlank(message = "notes are required")
        @Size(min = 10, max = 2000, message = "notes must be between 10 and 2000 characters")
        String notes) {
}
