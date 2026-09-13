package dev.mstefanov.hms.api.v1;

import dev.mstefanov.hms.api.v1.dto.AppointmentConfirmRequest;
import dev.mstefanov.hms.api.v1.dto.AppointmentRequest;
import dev.mstefanov.hms.api.v1.dto.AppointmentResponse;
import dev.mstefanov.hms.model.Appointment;
import dev.mstefanov.hms.model.service.AppointmentServiceModel;
import dev.mstefanov.hms.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/appointments", produces = "application/json")
@Tag(name = "Appointments", description = "Patients request, doctors confirm and archive")
public class AppointmentApiController {

    private final AppointmentService appointmentService;

    public AppointmentApiController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @Operation(summary = "List appointments visible to the caller",
            description = "Patients see their own, doctors the ones assigned to them, admins all.")
    public List<AppointmentResponse> list(Authentication authentication) {
        List<AppointmentServiceModel> appointments;
        if (Roles.has(authentication, Roles.ADMIN)) {
            appointments = appointmentService.getAllAppointments();
        } else if (Roles.has(authentication, Roles.DOCTOR)) {
            appointments = appointmentService.getAppointmentsForDoctor(authentication.getName());
        } else {
            appointments = appointmentService.getAppointmentsForUserWithUsername(authentication.getName());
        }
        return appointments.stream().map(AppointmentResponse::from).toList();
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Request an appointment with a doctor (patient)")
    @ApiResponse(responseCode = "201", description = "Appointment requested")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Doctor not found")
    public ResponseEntity<AppointmentResponse> request(@Valid @RequestBody AppointmentRequest body,
                                                       Authentication authentication) {
        AppointmentResponse created = AppointmentResponse.from(
                appointmentService.createAppointmentRequest(body.doctorUsername(), authentication.getName()));
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(created.id()))
                .body(created);
    }

    @PostMapping(value = "/{id}/confirm", consumes = "application/json")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Confirm a requested appointment at a given time (doctor)")
    @ApiResponse(responseCode = "200", description = "Appointment confirmed")
    @ApiResponse(responseCode = "403", description = "Appointment belongs to another doctor")
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    @ApiResponse(responseCode = "409", description = "Appointment is not in REQUESTED state")
    public AppointmentResponse confirm(@PathVariable Long id,
                                       @Valid @RequestBody AppointmentConfirmRequest body,
                                       Authentication authentication) {
        assertOwnedByDoctor(id, authentication);
        return AppointmentResponse.from(appointmentService.confirmAppointment(id, body.appointmentTime()));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Archive an appointment (doctor); idempotent")
    @ApiResponse(responseCode = "200", description = "Appointment archived")
    @ApiResponse(responseCode = "403", description = "Appointment belongs to another doctor")
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    public AppointmentResponse archive(@PathVariable Long id, Authentication authentication) {
        assertOwnedByDoctor(id, authentication);
        return AppointmentResponse.from(appointmentService.archiveAppointment(id));
    }

    /** Admins may act on any appointment; a doctor only on appointments assigned to them. */
    private void assertOwnedByDoctor(Long id, Authentication authentication) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        if (!Roles.has(authentication, Roles.ADMIN)
                && !appointment.getDoctor().getUsername().equals(authentication.getName())) {
            throw new AccessDeniedException("Appointment " + id + " is assigned to another doctor");
        }
    }
}
