package dev.mstefanov.hms.service.impl;

import dev.mstefanov.hms.exception.ConflictException;
import dev.mstefanov.hms.exception.NotFoundException;
import dev.mstefanov.hms.model.Appointment;
import dev.mstefanov.hms.model.service.AppointmentServiceModel;
import dev.mstefanov.hms.repository.AppointmentRepository;
import dev.mstefanov.hms.service.AppointmentService;
import dev.mstefanov.hms.service.StatusService;
import dev.mstefanov.hms.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    static final String REQUESTED = "REQUESTED";
    static final String CONFIRMED = "CONFIRMED";
    static final String ARCHIVED = "ARCHIVED";

    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final StatusService statusService;
    private final ModelMapper modelMapper;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, UserService userService,
                                  StatusService statusService, ModelMapper modelMapper) {
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.statusService = statusService;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<AppointmentServiceModel> getAppointmentsForUserWithUsername(String patientName) {
        return map(appointmentRepository.findAllByPatientUsername(patientName));
    }

    @Override
    public List<AppointmentServiceModel> getAppointmentsForDoctor(String doctorUsername) {
        return map(appointmentRepository.findAllByDoctorUsername(doctorUsername));
    }

    @Override
    public List<AppointmentServiceModel> getAllAppointments() {
        return map(appointmentRepository.findAll());
    }

    @Override
    @Transactional
    public AppointmentServiceModel createAppointmentRequest(String doctorUsername, String patientName) {
        Appointment appointment = new Appointment(
                userService.getUserByUsername(doctorUsername),
                userService.getUserByUsername(patientName),
                null,
                statusService.getRequestedStatus());
        return map(appointmentRepository.save(appointment));
    }

    @Override
    public List<AppointmentServiceModel> getAllRequestedAppointmentsByDoctor(String username) {
        return map(appointmentRepository.findAllByDoctorUsernameAndStatusName(username, REQUESTED));
    }

    @Override
    public List<AppointmentServiceModel> getAllConfirmedAppointmentsByDoctor(String username) {
        return map(appointmentRepository.findAllByDoctorUsernameAndStatusName(username, CONFIRMED));
    }

    @Override
    public List<AppointmentServiceModel> getAllArchivedAppointmentsByDoctor(String username) {
        return map(appointmentRepository.findAllByDoctorUsernameAndStatusName(username, ARCHIVED));
    }

    @Override
    @Transactional
    public AppointmentServiceModel confirmAppointment(Long id, LocalDateTime appointmentTime) {
        Appointment appointment = getAppointmentById(id);
        if (!REQUESTED.equals(appointment.getStatus().getName())) {
            throw new ConflictException("Appointment " + id + " is " + appointment.getStatus().getName()
                    + " and can no longer be confirmed");
        }
        appointment.setStatus(statusService.getConfirmedStatus());
        appointment.setAppointmentTime(appointmentTime);
        return map(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentServiceModel archiveAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        if (!ARCHIVED.equals(appointment.getStatus().getName())) {
            appointment.setStatus(statusService.getArchivedStatus());
            appointment = appointmentRepository.save(appointment);
        }
        return map(appointment);
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Appointment", id));
    }

    private AppointmentServiceModel map(Appointment appointment) {
        return modelMapper.map(appointment, AppointmentServiceModel.class);
    }

    private List<AppointmentServiceModel> map(List<Appointment> appointments) {
        return appointments.stream().map(this::map).toList();
    }
}
