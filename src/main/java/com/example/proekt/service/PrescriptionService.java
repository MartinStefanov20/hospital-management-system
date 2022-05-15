package com.example.proekt.service;

import com.example.proekt.model.binding.PrescriptionBindingModel;
import com.example.proekt.model.binding.PrescriptionEditBindingModel;
import com.example.proekt.model.service.PrescriptionServiceModel;

import java.util.List;

public interface PrescriptionService {

    List<PrescriptionServiceModel> getPrescriptionsForUser(String patientName);

    PrescriptionServiceModel getPrescriptionWithId(Long id);

    void issuePrescription(PrescriptionBindingModel prescriptionBindingModel);

    List<PrescriptionServiceModel> getPrescriptionsForDoctor(String doctorName);

    void editPrescription(PrescriptionEditBindingModel prescriptionEditBindingModel);
}
