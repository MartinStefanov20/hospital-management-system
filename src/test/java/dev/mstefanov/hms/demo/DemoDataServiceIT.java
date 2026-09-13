package dev.mstefanov.hms.demo;

import dev.mstefanov.hms.repository.AppointmentRepository;
import dev.mstefanov.hms.repository.DepartmentRepository;
import dev.mstefanov.hms.repository.PrescriptionRepository;
import dev.mstefanov.hms.repository.RoleRepository;
import dev.mstefanov.hms.repository.StatusRepository;
import dev.mstefanov.hms.repository.UserRepository;
import dev.mstefanov.hms.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class DemoDataServiceIT {

    @Autowired
    DemoDataService demoDataService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    DepartmentRepository departmentRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    PrescriptionRepository prescriptionRepository;
    @Autowired
    StatusRepository statusRepository;

    @Test
    void resetTwiceIsIdempotent() {
        demoDataService.reset();
        Counts first = Counts.now(this);

        demoDataService.reset();
        Counts second = Counts.now(this);

        assertThat(first).isEqualTo(second);
        assertThat(first.users).isEqualTo(3);
        assertThat(first.roles).isEqualTo(3);
        assertThat(first.departments).isEqualTo(4);
        assertThat(first.appointments).isEqualTo(6);
        assertThat(first.prescriptions).isEqualTo(2);
        assertThat(first.statuses).as("statuses are Flyway reference data and must survive a reset").isEqualTo(3);
        assertThat(userRepository.findByUsername("dr.house")).isPresent();
    }

    @Test
    void seedIfEmptyDoesNothingWhenUsersExist() {
        demoDataService.reset();
        long appointmentsBefore = appointmentRepository.count();

        demoDataService.seedIfEmpty();

        assertThat(appointmentRepository.count()).isEqualTo(appointmentsBefore);
    }

    record Counts(long users, long roles, long departments, long appointments, long prescriptions, long statuses) {
        static Counts now(DemoDataServiceIT t) {
            return new Counts(t.userRepository.count(), t.roleRepository.count(), t.departmentRepository.count(),
                    t.appointmentRepository.count(), t.prescriptionRepository.count(), t.statusRepository.count());
        }
    }
}
