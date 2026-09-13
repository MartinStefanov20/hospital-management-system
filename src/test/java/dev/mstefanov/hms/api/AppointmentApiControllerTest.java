package dev.mstefanov.hms.api;

import dev.mstefanov.hms.api.v1.AppointmentApiController;
import dev.mstefanov.hms.configurations.ApplicationBeanConfiguration;
import dev.mstefanov.hms.configurations.SecurityConfig;
import dev.mstefanov.hms.exception.ConflictException;
import dev.mstefanov.hms.exception.NotFoundException;
import dev.mstefanov.hms.model.service.AppointmentServiceModel;
import dev.mstefanov.hms.model.service.StatusServiceModel;
import dev.mstefanov.hms.model.view.AppointmentUserViewModel;
import dev.mstefanov.hms.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web slice: real security chains + method security + API advice, mocked service layer. */
@WebMvcTest(AppointmentApiController.class)
@Import({SecurityConfig.class, ApplicationBeanConfiguration.class})
class AppointmentApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AppointmentService appointmentService;

    @MockitoBean
    UserDetailsService userDetailsService;

    @Test
    void invalidBodyIs400ProblemDetailWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .with(user("patient").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorUsername\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/v1/appointments"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("doctorUsername")));
        verify(appointmentService, never()).createAppointmentRequest(any(), any());
    }

    @Test
    void validBodyIs201WithLocationAndBody() throws Exception {
        when(appointmentService.createAppointmentRequest("dr.house", "patient")).thenReturn(appointment(42L, "REQUESTED"));

        mockMvc.perform(post("/api/v1/appointments")
                        .with(user("patient").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorUsername\":\"dr.house\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/appointments/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.doctor.username").value("dr.house"))
                .andExpect(jsonPath("$.appointmentTime").isEmpty());
    }

    @Test
    void doctorMayNotRequestAppointments() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .with(user("dr.house").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorUsername\":\"dr.house\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void anonymousIs401() throws Exception {
        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));
    }

    @Test
    void listIsRoleAware() throws Exception {
        when(appointmentService.getAllAppointments()).thenReturn(List.of(appointment(1L, "REQUESTED"), appointment(2L, "ARCHIVED")));
        when(appointmentService.getAppointmentsForDoctor("dr.house")).thenReturn(List.of(appointment(1L, "REQUESTED")));
        when(appointmentService.getAppointmentsForUserWithUsername("patient")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/appointments").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/v1/appointments").with(user("dr.house").roles("DOCTOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/appointments").with(user("patient").roles("PATIENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void confirmUnknownAppointmentIs404ProblemDetail() throws Exception {
        when(appointmentService.getAppointmentById(99L)).thenThrow(NotFoundException.of("Appointment", 99L));

        mockMvc.perform(post("/api/v1/appointments/99/confirm")
                        .with(user("dr.house").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentTime\":\"2030-01-01T10:00:00\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Appointment 99 not found"));
    }

    @Test
    void confirmingNonRequestedAppointmentIs409() throws Exception {
        dev.mstefanov.hms.model.User doctor = new dev.mstefanov.hms.model.User();
        doctor.setUsername("dr.house");
        dev.mstefanov.hms.model.Appointment entity = new dev.mstefanov.hms.model.Appointment();
        entity.setDoctor(doctor);
        when(appointmentService.getAppointmentById(7L)).thenReturn(entity);
        when(appointmentService.confirmAppointment(eq(7L), any(LocalDateTime.class)))
                .thenThrow(new ConflictException("Appointment 7 is CONFIRMED and can no longer be confirmed"));

        mockMvc.perform(post("/api/v1/appointments/7/confirm")
                        .with(user("dr.house").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentTime\":\"2030-01-01T10:00:00\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    private static AppointmentServiceModel appointment(Long id, String status) {
        AppointmentServiceModel model = new AppointmentServiceModel();
        model.setId(id);
        model.setDoctor(new AppointmentUserViewModel(10L, "dr.house", "Gregory", "House", "Dr."));
        model.setPatient(new AppointmentUserViewModel(20L, "patient", "John", "Doe", "Mr."));
        StatusServiceModel statusModel = new StatusServiceModel(status, status.toLowerCase());
        model.setStatus(statusModel);
        return model;
    }
}
