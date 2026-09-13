package dev.mstefanov.hms.repository;

import dev.mstefanov.hms.model.Appointment;
import dev.mstefanov.hms.model.User;
import dev.mstefanov.hms.support.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static dev.mstefanov.hms.repository.RepositoryTestSupport.status;
import static dev.mstefanov.hms.repository.RepositoryTestSupport.user;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class AppointmentRepositoryIT {

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    TestEntityManager em;

    User doctor;
    User otherDoctor;
    User patient;

    @BeforeEach
    void seed() {
        doctor = user(em, "doc.one", "ROLE_DOCTOR");
        otherDoctor = user(em, "doc.two", "ROLE_DOCTOR");
        patient = user(em, "pat.one", "ROLE_PATIENT");

        em.persist(new Appointment(doctor, patient, null, status(em, "REQUESTED")));
        em.persist(new Appointment(doctor, patient, LocalDateTime.now().plusDays(1), status(em, "CONFIRMED")));
        em.persist(new Appointment(otherDoctor, patient, null, status(em, "REQUESTED")));
        em.flush();
        em.clear();
    }

    @Test
    void findAllByPatientUsername_returnsEveryAppointmentOfThePatient() {
        assertThat(appointmentRepository.findAllByPatientUsername("pat.one")).hasSize(3);
        assertThat(appointmentRepository.findAllByPatientUsername("nobody")).isEmpty();
    }

    @Test
    void findAllByDoctorUsername_returnsOnlyThatDoctorsAppointments() {
        assertThat(appointmentRepository.findAllByDoctorUsername("doc.one"))
                .hasSize(2)
                .allSatisfy(a -> assertThat(a.getDoctor().getUsername()).isEqualTo("doc.one"));
    }

    @Test
    void findAllByDoctorUsernameAndStatusName_filtersByStatus() {
        assertThat(appointmentRepository.findAllByDoctorUsernameAndStatusName("doc.one", "REQUESTED"))
                .singleElement()
                .satisfies(a -> assertThat(a.getAppointmentTime()).isNull());
        assertThat(appointmentRepository.findAllByDoctorUsernameAndStatusName("doc.one", "ARCHIVED")).isEmpty();
    }
}
