package dev.mstefanov.hms.demo;

import dev.mstefanov.hms.model.Appointment;
import dev.mstefanov.hms.model.Department;
import dev.mstefanov.hms.model.Prescription;
import dev.mstefanov.hms.model.Role;
import dev.mstefanov.hms.model.Status;
import dev.mstefanov.hms.model.User;
import dev.mstefanov.hms.repository.AppointmentRepository;
import dev.mstefanov.hms.repository.DepartmentRepository;
import dev.mstefanov.hms.repository.PrescriptionRepository;
import dev.mstefanov.hms.repository.RoleRepository;
import dev.mstefanov.hms.repository.StatusRepository;
import dev.mstefanov.hms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds (and can wipe and re-seed) a small, deterministic demo data set:
 * the accounts from {@code app.demo.accounts}, a few departments, appointments in every status
 * and two prescriptions. Statuses themselves are reference data owned by Flyway (V2) and are never touched.
 */
@Service
public class DemoDataService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final StatusRepository statusRepository;
    private final PasswordEncoder passwordEncoder;
    private final DemoProperties demoProperties;

    public DemoDataService(UserRepository userRepository, RoleRepository roleRepository,
                           DepartmentRepository departmentRepository, AppointmentRepository appointmentRepository,
                           PrescriptionRepository prescriptionRepository, StatusRepository statusRepository,
                           PasswordEncoder passwordEncoder, DemoProperties demoProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.statusRepository = statusRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoProperties = demoProperties;
    }

    /** Seeds the demo data set only when there are no users yet. */
    @Transactional
    public void seedIfEmpty() {
        if (userRepository.count() > 0) {
            log.info("Demo data: users already present, skipping seed");
            return;
        }
        seed();
    }

    /** Deletes all user-generated data (in FK order) and seeds the demo data set again. */
    @Transactional
    public void reset() {
        log.info("Demo data: resetting");
        prescriptionRepository.deleteAllInBatch();
        appointmentRepository.deleteAllInBatch();
        departmentRepository.deleteAll();          // entity delete so departments_doctors rows go too
        departmentRepository.flush();              // ...and must hit the DB before users are bulk-deleted
        roleRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        seed();
    }

    private void seed() {
        Map<String, User> users = seedUsers();
        User doctor = firstWithRole(users, "ROLE_DOCTOR");
        User patient = firstWithRole(users, "ROLE_PATIENT");

        seedDepartments(doctor);
        List<Appointment> archived = seedAppointments(doctor, patient);
        seedPrescriptions(doctor, patient, archived);

        log.info("Demo data: seeded {} users, {} departments, {} appointments, {} prescriptions",
                users.size(), departmentRepository.count(), appointmentRepository.count(), prescriptionRepository.count());
    }

    private Map<String, User> seedUsers() {
        if (demoProperties.getAccounts().isEmpty()) {
            throw new IllegalStateException("app.demo.accounts is empty; nothing to seed");
        }

        Map<String, User> users = new LinkedHashMap<>();
        for (DemoProperties.Account account : demoProperties.getAccounts()) {
            User user = new User();
            user.setUsername(account.getUsername());
            user.setPassword(passwordEncoder.encode(account.getPassword()));
            user.setEnabled(true);
            user.setRegistrationDate(LocalDateTime.now());
            user.setRating(4.5);
            applyProfile(user, account.getRole());

            Role role = new Role();
            role.setName("ROLE_" + account.getRole().toUpperCase());
            role.setUser(user);
            user.setRoles(new ArrayList<>(List.of(role)));

            users.put(user.getUsername(), userRepository.save(user));
        }
        return users;
    }

    private static void applyProfile(User user, String role) {
        switch (role.toUpperCase()) {
            case "ADMIN" -> {
                user.setSalutation("Mr.");
                user.setFirstName("Alex");
                user.setLastName("Adminov");
                user.setBirthday(LocalDate.of(1980, 1, 1));
            }
            case "DOCTOR" -> {
                user.setSalutation("Dr.");
                user.setFirstName("Gregory");
                user.setLastName("House");
                user.setBirthday(LocalDate.of(1959, 6, 11));
            }
            default -> {
                user.setSalutation("Mr.");
                user.setFirstName("John");
                user.setLastName("Doe");
                user.setBirthday(LocalDate.of(1990, 5, 20));
            }
        }
    }

    private static User firstWithRole(Map<String, User> users, String roleName) {
        return users.values().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> roleName.equals(r.getName())))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("app.demo.accounts has no account with role " + roleName));
    }

    private void seedDepartments(User doctor) {
        List<Department> departments = new ArrayList<>();
        for (String[] d : new String[][]{
                {"Cardiology", "Diagnosis and treatment of heart and blood vessel conditions, from routine check-ups to cardiac rehabilitation."},
                {"Neurology", "Care for disorders of the brain, spinal cord and nervous system, including headaches, epilepsy and stroke follow-up."},
                {"Orthopedics", "Treatment of bones, joints, ligaments and muscles: fractures, sports injuries and joint replacement."},
                {"Pediatrics", "Preventive and acute care for infants, children and adolescents."}}) {
            Department department = new Department(d[0], d[1], new ArrayList<>(List.of(doctor)));
            departments.add(department);
        }
        departmentRepository.saveAll(departments);
    }

    private List<Appointment> seedAppointments(User doctor, User patient) {
        Status requested = status("REQUESTED");
        Status confirmed = status("CONFIRMED");
        Status archivedStatus = status("ARCHIVED");
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        List<Appointment> appointments = List.of(
                new Appointment(doctor, patient, null, requested),
                new Appointment(doctor, patient, null, requested),
                new Appointment(doctor, patient, now.plusDays(2).withHour(10), confirmed),
                new Appointment(doctor, patient, now.plusDays(7).withHour(14), confirmed),
                new Appointment(doctor, patient, now.minusDays(30).withHour(9), archivedStatus),
                new Appointment(doctor, patient, now.minusDays(10).withHour(11), archivedStatus));
        appointmentRepository.saveAll(appointments);

        return appointments.stream().filter(a -> a.getStatus() == archivedStatus).toList();
    }

    private void seedPrescriptions(User doctor, User patient, List<Appointment> archived) {
        String[] notes = {
                "Ibuprofen 400 mg every 8 hours for 5 days with food. Rest the knee and apply ice twice daily.",
                "Amoxicillin 500 mg three times daily for 7 days. Return if fever persists beyond 48 hours."};
        List<Prescription> prescriptions = new ArrayList<>();
        for (int i = 0; i < notes.length; i++) {
            Prescription prescription = new Prescription();
            prescription.setPrescribedBy(doctor);
            prescription.setPrescribeTo(patient);
            prescription.setDate(LocalDate.now());
            prescription.setPrescriptionNotes(notes[i]);
            prescription.setAppointment(i < archived.size() ? archived.get(i) : null);
            prescriptions.add(prescription);
        }
        prescriptionRepository.saveAll(prescriptions);
    }

    private Status status(String name) {
        return statusRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Status " + name + " missing; has Flyway migration V2 run?"));
    }
}
