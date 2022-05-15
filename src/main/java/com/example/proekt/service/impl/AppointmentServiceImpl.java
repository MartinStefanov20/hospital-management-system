package com.example.proekt.service.impl;

import com.example.proekt.model.Appointment;
import com.example.proekt.model.service.AppointmentServiceModel;
import com.example.proekt.repository.AppointmentRepository;
import com.example.proekt.service.AppointmentService;
import com.example.proekt.service.StatusService;
import com.example.proekt.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final StatusService statusService;
    private final ModelMapper modelMapper;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, UserService userService, StatusService statusService, ModelMapper modelMapper) {
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.statusService = statusService;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<AppointmentServiceModel> getAppointmentsForUserWithUsername(String patientName) {

        List<AppointmentServiceModel> appointments = new ArrayList<>();

        for (Appointment appointment : this.appointmentRepository.findAllByPatientUsername(patientName)) {
            appointments.add(this.modelMapper.map(appointment, AppointmentServiceModel.class));
        }

        return appointments;
    }

    @Override
    public void createAppointmentRequest(String doctorUsername, String patientName) {

        Appointment appointment = new Appointment(
                this.userService.getUserByUsername(doctorUsername), this.userService.getUserByUsername(patientName),
                null, this.statusService.getRequestedStatus());

        this.appointmentRepository.save(appointment);

    }

    @Override
    public List<AppointmentServiceModel> getAllRequestedAppointmentsByDoctor(String username) {

        List<AppointmentServiceModel> requestedAppointments = new ArrayList<>();

        for (Appointment appointment : this.appointmentRepository.findAllByDoctorUsernameAndStatusName(username, "REQUESTED")) {
            requestedAppointments.add(this.modelMapper.map(appointment, AppointmentServiceModel.class));
        }

        return requestedAppointments;
    }

    @Override
    public List<AppointmentServiceModel> getAllConfirmedAppointmentsByDoctor(String username) {

        List<AppointmentServiceModel> requestedAppointments = new ArrayList<>();

        for (Appointment appointment : this.appointmentRepository.findAllByDoctorUsernameAndStatusName(username, "CONFIRMED")) {
            requestedAppointments.add(this.modelMapper.map(appointment, AppointmentServiceModel.class));
        }

        return requestedAppointments;
    }

    @Override
    public List<AppointmentServiceModel> getAllArchivedAppointmentsByDoctor(String username) {

        List<AppointmentServiceModel> requestedAppointments = new ArrayList<>();

        for (Appointment appointment : this.appointmentRepository.findAllByDoctorUsernameAndStatusName(username, "ARCHIVED")) {
            requestedAppointments.add(this.modelMapper.map(appointment, AppointmentServiceModel.class));
        }

        return requestedAppointments;
    }

    @Override
    @Transactional
    public void confirmAppointment(String id, String dateAndTime) {

        Long appointmentID = Long.parseLong(id);

        String[] dateTokens = dateAndTime.split("T");
        dateTokens[1] = dateTokens[1].substring(0, 5);
        dateAndTime = dateTokens[0] + " " + dateTokens[1];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateAndTime, formatter);

        Appointment appointment = this.appointmentRepository.getOne(appointmentID);
        appointment.setStatus(this.statusService.getConfirmedStatus());
        appointment.setAppointmentTime(dateTime);

        this.appointmentRepository.save(appointment);

    }

    @Transactional
    @Override
    public void archiveAppointment(Long id) {

        Appointment appointment = this.appointmentRepository.getOne(id);
        appointment.setStatus(this.statusService.getArchivedStatus());
        this.appointmentRepository.save(appointment);

    }

    @Override
    public Appointment getAppointmentById(Long id) {

        return this.appointmentRepository.findOneById(id);

    }

    @Override
    public void initAppointments() {
        if (this.appointmentRepository.count() == 0) {
            Appointment firstAppointment =
                    new Appointment(this.userService.getUserByUsername("martin.doctor"),
                            this.userService.getUserByUsername("martin.stefanov"),
                            LocalDateTime.now(), this.statusService.getRequestedStatus());
            Appointment secondAppointment = new Appointment(this.userService.getUserByUsername("martin.doctor"),
                    this.userService.getUserByUsername("martin.stefanov"),
                    LocalDateTime.now(), this.statusService.getConfirmedStatus());
            Appointment thirdAppointment = new Appointment(this.userService.getUserByUsername("martin.doctor"),
                    this.userService.getUserByUsername("martin.stefanov"),
                    LocalDateTime.now(), this.statusService.getArchivedStatus());

            this.appointmentRepository.saveAll(List.of(firstAppointment, secondAppointment, thirdAppointment));

        }
    }
}
