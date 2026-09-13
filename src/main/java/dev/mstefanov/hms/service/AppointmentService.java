package dev.mstefanov.hms.service;

import dev.mstefanov.hms.model.Appointment;
import dev.mstefanov.hms.model.service.AppointmentServiceModel;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {

    List<AppointmentServiceModel> getAppointmentsForUserWithUsername(String patientName);

    List<AppointmentServiceModel> getAppointmentsForDoctor(String doctorUsername);

    List<AppointmentServiceModel> getAllAppointments();

    AppointmentServiceModel createAppointmentRequest(String doctorUsername, String patientName);

    List<AppointmentServiceModel> getAllRequestedAppointmentsByDoctor(String username);

    List<AppointmentServiceModel> getAllConfirmedAppointmentsByDoctor(String username);

    List<AppointmentServiceModel> getAllArchivedAppointmentsByDoctor(String username);

    /** Moves a REQUESTED appointment to CONFIRMED at the given time. */
    AppointmentServiceModel confirmAppointment(Long id, LocalDateTime appointmentTime);

    /** Moves an appointment to ARCHIVED; archiving an already archived appointment is a no-op. */
    AppointmentServiceModel archiveAppointment(Long id);

    Appointment getAppointmentById(Long id);
}
