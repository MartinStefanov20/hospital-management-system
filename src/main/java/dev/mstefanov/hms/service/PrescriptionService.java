package dev.mstefanov.hms.service;

import dev.mstefanov.hms.model.binding.PrescriptionBindingModel;
import dev.mstefanov.hms.model.binding.PrescriptionEditBindingModel;
import dev.mstefanov.hms.model.service.PrescriptionServiceModel;

import java.util.List;

public interface PrescriptionService {

    List<PrescriptionServiceModel> getPrescriptionsForUser(String patientName);

    PrescriptionServiceModel getPrescriptionWithId(Long id);

    void issuePrescription(PrescriptionBindingModel prescriptionBindingModel);

    List<PrescriptionServiceModel> getPrescriptionsForDoctor(String doctorName);

    void editPrescription(PrescriptionEditBindingModel prescriptionEditBindingModel);
}
