package dev.mstefanov.hms.service.impl;

import dev.mstefanov.hms.exception.ConflictException;
import dev.mstefanov.hms.exception.NotFoundException;
import dev.mstefanov.hms.model.Appointment;
import dev.mstefanov.hms.model.Status;
import dev.mstefanov.hms.model.User;
import dev.mstefanov.hms.model.service.AppointmentServiceModel;
import dev.mstefanov.hms.repository.AppointmentRepository;
import dev.mstefanov.hms.service.StatusService;
import dev.mstefanov.hms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    AppointmentRepository appointmentRepository;
    @Mock
    UserService userService;
    @Mock
    StatusService statusService;
    @Spy
    ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    AppointmentServiceImpl service;

    Status requested = status(1L, "REQUESTED");
    Status confirmed = status(2L, "CONFIRMED");
    Status archived = status(3L, "ARCHIVED");
    User doctor = user(10L, "dr.house");
    User patient = user(20L, "patient");

    @BeforeEach
    void saveReturnsArgument() {
        lenient().when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createAppointmentRequest_linksDoctorPatientAndRequestedStatus() {
        when(userService.getUserByUsername("dr.house")).thenReturn(doctor);
        when(userService.getUserByUsername("patient")).thenReturn(patient);
        when(statusService.getRequestedStatus()).thenReturn(requested);

        AppointmentServiceModel result = service.createAppointmentRequest("dr.house", "patient");

        ArgumentCaptor<Appointment> saved = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(saved.capture());
        assertThat(saved.getValue().getDoctor()).isSameAs(doctor);
        assertThat(saved.getValue().getPatient()).isSameAs(patient);
        assertThat(saved.getValue().getStatus()).isSameAs(requested);
        assertThat(saved.getValue().getAppointmentTime()).isNull();
        assertThat(result.getDoctor().getUsername()).isEqualTo("dr.house");
        assertThat(result.getStatus().getName()).isEqualTo("REQUESTED");
    }

    @Test
    void confirmAppointment_movesRequestedToConfirmedWithTime() {
        Appointment appointment = new Appointment(doctor, patient, null, requested);
        appointment.setId(5L);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));
        when(statusService.getConfirmedStatus()).thenReturn(confirmed);
        LocalDateTime time = LocalDateTime.of(2030, 1, 15, 10, 30);

        AppointmentServiceModel result = service.confirmAppointment(5L, time);

        assertThat(appointment.getStatus()).isSameAs(confirmed);
        assertThat(appointment.getAppointmentTime()).isEqualTo(time);
        assertThat(result.getStatus().getName()).isEqualTo("CONFIRMED");
    }

    @Test
    void confirmAppointment_rejectsNonRequestedWithConflict() {
        Appointment appointment = new Appointment(doctor, patient, LocalDateTime.now(), confirmed);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.confirmAppointment(5L, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CONFIRMED");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void confirmAppointment_unknownIdIsNotFound() {
        when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmAppointment(404L, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void archiveAppointment_isIdempotent() {
        Appointment appointment = new Appointment(doctor, patient, LocalDateTime.now(), archived);
        when(appointmentRepository.findById(7L)).thenReturn(Optional.of(appointment));

        AppointmentServiceModel result = service.archiveAppointment(7L);

        verify(appointmentRepository, never()).save(any());
        verify(statusService, never()).getArchivedStatus();
        assertThat(result.getStatus().getName()).isEqualTo("ARCHIVED");
    }

    @Test
    void archiveAppointment_archivesConfirmed() {
        Appointment appointment = new Appointment(doctor, patient, LocalDateTime.now(), confirmed);
        when(appointmentRepository.findById(7L)).thenReturn(Optional.of(appointment));
        when(statusService.getArchivedStatus()).thenReturn(archived);

        service.archiveAppointment(7L);

        assertThat(appointment.getStatus()).isSameAs(archived);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void doctorListingsUseStatusFilteredQueries() {
        when(appointmentRepository.findAllByDoctorUsernameAndStatusName("dr.house", "REQUESTED"))
                .thenReturn(List.of(new Appointment(doctor, patient, null, requested)));

        assertThat(service.getAllRequestedAppointmentsByDoctor("dr.house"))
                .singleElement()
                .satisfies(a -> assertThat(a.getPatient().getUsername()).isEqualTo("patient"));
    }

    private static Status status(Long id, String name) {
        Status status = new Status(name, name.toLowerCase());
        status.setId(id);
        return status;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setSalutation("Dr.");
        user.setRoles(List.of());
        return user;
    }
}
