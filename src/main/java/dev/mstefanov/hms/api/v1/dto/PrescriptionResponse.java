package dev.mstefanov.hms.api.v1.dto;

import dev.mstefanov.hms.model.service.PrescriptionServiceModel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "A prescription issued by a doctor to a patient")
public record PrescriptionResponse(
        @Schema(example = "5") Long id,
        UserSummary doctor,
        UserSummary patient,
        @Schema(example = "2026-09-13") LocalDate date,
        String notes,
        @Schema(description = "Appointment the prescription was issued for, if any", nullable = true, example = "17")
        Long appointmentId) {

    public static PrescriptionResponse from(PrescriptionServiceModel prescription) {
        return new PrescriptionResponse(
                prescription.getId(),
                UserSummary.from(prescription.getPrescribedBy()),
                UserSummary.from(prescription.getPrescribeTo()),
                prescription.getDate(),
                prescription.getPrescriptionNotes(),
                prescription.getAppointmentId());
    }
}
