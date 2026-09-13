package dev.mstefanov.hms.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Patient's request for an appointment with a doctor")
public record AppointmentRequest(
        @Schema(example = "dr.house")
        @NotBlank(message = "doctorUsername is required")
        @Size(min = 4, max = 30, message = "doctorUsername must be between 4 and 30 characters")
        String doctorUsername) {
}
