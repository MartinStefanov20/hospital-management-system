package dev.mstefanov.hms.api.v1;

import dev.mstefanov.hms.api.v1.dto.PrescriptionRequest;
import dev.mstefanov.hms.api.v1.dto.PrescriptionResponse;
import dev.mstefanov.hms.model.service.PrescriptionServiceModel;
import dev.mstefanov.hms.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/prescriptions", produces = "application/json")
@Tag(name = "Prescriptions", description = "Doctors issue, patients read")
public class PrescriptionApiController {

    private final PrescriptionService prescriptionService;

    public PrescriptionApiController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    @Operation(summary = "List prescriptions visible to the caller",
            description = "Patients see prescriptions issued to them, doctors the ones they issued, admins all.")
    public List<PrescriptionResponse> list(Authentication authentication) {
        List<PrescriptionServiceModel> prescriptions;
        if (Roles.has(authentication, Roles.ADMIN)) {
            prescriptions = prescriptionService.getAllPrescriptions();
        } else if (Roles.has(authentication, Roles.DOCTOR)) {
            prescriptions = prescriptionService.getPrescriptionsForDoctor(authentication.getName());
        } else {
            prescriptions = prescriptionService.getPrescriptionsForUser(authentication.getName());
        }
        return prescriptions.stream().map(PrescriptionResponse::from).toList();
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Issue a prescription (doctor)")
    @ApiResponse(responseCode = "201", description = "Prescription issued")
    @ApiResponse(responseCode = "400", description = "Validation failed or appointment does not belong to the patient")
    @ApiResponse(responseCode = "404", description = "Patient or appointment not found")
    public ResponseEntity<PrescriptionResponse> issue(@Valid @RequestBody PrescriptionRequest body,
                                                      Authentication authentication) {
        PrescriptionResponse created = PrescriptionResponse.from(prescriptionService.issuePrescription(
                authentication.getName(), body.patientId(), body.appointmentId(), body.notes()));
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(created.id()))
                .body(created);
    }
}
