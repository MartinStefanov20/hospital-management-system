package dev.mstefanov.hms.repository;

import dev.mstefanov.hms.model.Prescription;
import dev.mstefanov.hms.model.User;
import dev.mstefanov.hms.support.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static dev.mstefanov.hms.repository.RepositoryTestSupport.user;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PrescriptionRepositoryIT {

    @Autowired
    PrescriptionRepository prescriptionRepository;

    @Autowired
    TestEntityManager em;

    @BeforeEach
    void seed() {
        User doctor = user(em, "doc.one", "ROLE_DOCTOR");
        User patientA = user(em, "pat.a", "ROLE_PATIENT");
        User patientB = user(em, "pat.b", "ROLE_PATIENT");

        em.persist(prescription(doctor, patientA, "Two tablets a day for a week."));
        em.persist(prescription(doctor, patientA, "Rest and fluids; follow up in 5 days."));
        em.persist(prescription(doctor, patientB, "One capsule before breakfast."));
        em.flush();
        em.clear();
    }

    private static Prescription prescription(User doctor, User patient, String notes) {
        Prescription p = new Prescription();
        p.setPrescribedBy(doctor);
        p.setPrescribeTo(patient);
        p.setDate(LocalDate.now());
        p.setPrescriptionNotes(notes);
        return p;
    }

    @Test
    void findAllByPrescribeToUsername_returnsOnlyThePatientsPrescriptions() {
        assertThat(prescriptionRepository.findAllByPrescribeToUsername("pat.a")).hasSize(2);
        assertThat(prescriptionRepository.findAllByPrescribeToUsername("pat.b")).hasSize(1);
        assertThat(prescriptionRepository.findAllByPrescribeToUsername("nobody")).isEmpty();
    }

    @Test
    void findAllByPrescribedByUsername_returnsEverythingTheDoctorIssued() {
        assertThat(prescriptionRepository.findAllByPrescribedByUsername("doc.one"))
                .hasSize(3)
                .extracting(Prescription::getPrescriptionNotes)
                .contains("One capsule before breakfast.");
    }
}
