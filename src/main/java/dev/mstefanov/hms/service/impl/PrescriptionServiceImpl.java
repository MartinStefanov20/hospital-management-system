package dev.mstefanov.hms.service.impl;

import dev.mstefanov.hms.exception.NotFoundException;
import dev.mstefanov.hms.model.Appointment;
import dev.mstefanov.hms.model.Prescription;
import dev.mstefanov.hms.model.User;
import dev.mstefanov.hms.model.service.PrescriptionServiceModel;
import dev.mstefanov.hms.repository.PrescriptionRepository;
import dev.mstefanov.hms.service.AppointmentService;
import dev.mstefanov.hms.service.PrescriptionService;
import dev.mstefanov.hms.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final ModelMapper modelMapper;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository, UserService userService,
                                   AppointmentService appointmentService, ModelMapper modelMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<PrescriptionServiceModel> getPrescriptionsForUser(String patientName) {
        return map(prescriptionRepository.findAllByPrescribeToUsername(patientName));
    }

    @Override
    public List<PrescriptionServiceModel> getPrescriptionsForDoctor(String doctorName) {
        return map(prescriptionRepository.findAllByPrescribedByUsername(doctorName));
    }

    @Override
    public List<PrescriptionServiceModel> getAllPrescriptions() {
        return map(prescriptionRepository.findAll());
    }

    @Override
    public PrescriptionServiceModel getPrescriptionWithId(Long id) {
        return map(findPrescription(id));
    }

    @Override
    @Transactional
    public PrescriptionServiceModel issuePrescription(String doctorUsername, Long patientId, Long appointmentId, String notes) {
        User doctor = userService.getUserByUsername(doctorUsername);
        User patient = userService.getUserById(patientId);

        Appointment appointment = null;
        if (appointmentId != null) {
            appointment = appointmentService.getAppointmentById(appointmentId);
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                throw new IllegalArgumentException("Appointment " + appointmentId + " does not belong to patient " + patientId);
            }
            appointmentService.archiveAppointment(appointmentId);
        }

        Prescription prescription = new Prescription();
        prescription.setPrescribedBy(doctor);
        prescription.setPrescribeTo(patient);
        prescription.setDate(LocalDate.now());
        prescription.setPrescriptionNotes(notes);
        prescription.setAppointment(appointment);
        return map(prescriptionRepository.save(prescription));
    }

    @Override
    @Transactional
    public PrescriptionServiceModel editPrescription(Long prescriptionId, String notes) {
        Prescription prescription = findPrescription(prescriptionId);
        prescription.setPrescriptionNotes(notes);
        prescription.setDate(LocalDate.now());
        return map(prescriptionRepository.save(prescription));
    }

    private Prescription findPrescription(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Prescription", id));
    }

    private PrescriptionServiceModel map(Prescription prescription) {
        return modelMapper.map(prescription, PrescriptionServiceModel.class);
    }

    private List<PrescriptionServiceModel> map(List<Prescription> prescriptions) {
        return prescriptions.stream().map(this::map).toList();
    }
}
