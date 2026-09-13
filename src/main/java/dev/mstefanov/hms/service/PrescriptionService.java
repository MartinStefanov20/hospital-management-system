package dev.mstefanov.hms.service;

import dev.mstefanov.hms.model.service.PrescriptionServiceModel;

import java.util.List;

public interface PrescriptionService {

    List<PrescriptionServiceModel> getPrescriptionsForUser(String patientName);

    List<PrescriptionServiceModel> getPrescriptionsForDoctor(String doctorName);

    List<PrescriptionServiceModel> getAllPrescriptions();

    PrescriptionServiceModel getPrescriptionWithId(Long id);

    /**
     * Issues a prescription by {@code doctorUsername} to patient {@code patientId}. When {@code appointmentId} is
     * given the appointment must belong to that patient and is archived as part of the operation.
     */
    PrescriptionServiceModel issuePrescription(String doctorUsername, Long patientId, Long appointmentId, String notes);

    PrescriptionServiceModel editPrescription(Long prescriptionId, String notes);
}
