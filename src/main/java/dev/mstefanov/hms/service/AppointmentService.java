package dev.mstefanov.hms.service;


import dev.mstefanov.hms.model.Appointment;
import dev.mstefanov.hms.model.service.AppointmentServiceModel;

import java.util.List;

public interface AppointmentService{

    List<AppointmentServiceModel> getAppointmentsForUserWithUsername(String patientName);

    void createAppointmentRequest(String doctorUsername, String patientName);

    List<AppointmentServiceModel> getAllRequestedAppointmentsByDoctor(String username);

    List<AppointmentServiceModel> getAllConfirmedAppointmentsByDoctor(String username);

    List<AppointmentServiceModel> getAllArchivedAppointmentsByDoctor(String username);

    void confirmAppointment(String id, String dateAndTime);

    void archiveAppointment(Long id);

    Appointment getAppointmentById(Long id);

}
