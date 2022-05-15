package com.example.proekt.web;

import com.example.proekt.model.binding.PrescriptionBindingModel;
import com.example.proekt.model.binding.PrescriptionEditBindingModel;
import com.example.proekt.model.service.PrescriptionServiceModel;
import com.example.proekt.model.view.PrescriptionViewModel;
import com.example.proekt.service.AppointmentService;
import com.example.proekt.service.PrescriptionService;
import com.example.proekt.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final ModelMapper modelMapper;

    public PrescriptionController(PrescriptionService prescriptionService, ModelMapper modelMapper, UserService userService, AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/prescriptions")
    public String getAllPrescriptionsPerPatient(Model model) {

        String patientName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<PrescriptionViewModel> prescriptions = new ArrayList<>();

        for (PrescriptionServiceModel prescriptionServiceModel : this.prescriptionService.getPrescriptionsForUser(patientName)) {
            prescriptions.add(this.modelMapper.map(prescriptionServiceModel, PrescriptionViewModel.class));
        }

        model.addAttribute("prescriptions", prescriptions);

        return "prescriptions";
    }

    @RequestMapping(value = "/prescriptions/prescription")
    public String getPatientPrescription(@RequestParam("id") Long id, Model model) {

        PrescriptionViewModel prescription = this.modelMapper
                .map(this.prescriptionService.getPrescriptionWithId(id), PrescriptionViewModel.class);

        model.addAttribute("prescription", prescription);

        return "prescription";
    }

    @RequestMapping("/doctor/issuePrescription")
    public String archiveAppointmentAndIssuePrescription(@RequestParam(name = "appointmentId") String appointmentId,
                                                         @RequestParam Long patientId, Model model) {


        model.addAttribute("appointmentId", appointmentId);
        model.addAttribute("patientId", patientId);
        model.addAttribute("nameOfUser", this.userService.getUserFullName(patientId));
        this.appointmentService.archiveAppointment(Long.parseLong(appointmentId));

        return "doctors/prescription-form";
    }

    @PostMapping("/doctor/issuePrescription")
    public String issuePrescription(@ModelAttribute("prescriptionBindingModel") PrescriptionBindingModel prescriptionBindingModel) {

        prescriptionBindingModel.setDoctorUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        this.prescriptionService.issuePrescription(prescriptionBindingModel);

        return "redirect:/doctor/appointments/confirmed";
    }

    @GetMapping("/doctor/prescriptions")
    public String getAllPrescriptionsPerDoctor(Model model) {

        String doctorName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<PrescriptionViewModel> prescriptions = new ArrayList<>();

        for (PrescriptionServiceModel prescriptionServiceModel : this.prescriptionService.getPrescriptionsForDoctor(doctorName)) {
            prescriptions.add(this.modelMapper.map(prescriptionServiceModel, PrescriptionViewModel.class));
        }

        model.addAttribute("prescriptions", prescriptions);

        return "doctors/doctors-prescriptions";
    }


    @RequestMapping(value = "/doctor/prescription")
    public String getDoctorPrescription(@RequestParam("id") Long id, Model model) {


        PrescriptionViewModel prescription = this.modelMapper
                .map(this.prescriptionService.getPrescriptionWithId(id), PrescriptionViewModel.class);

        model.addAttribute("prescription", prescription);

        return "doctors/doctors-prescription";
    }


    @GetMapping("/doctor/editPrescription")
    public String getEditPrescription (@RequestParam("id") Long id, Model model){

        PrescriptionViewModel prescription = this.modelMapper
                .map(this.prescriptionService.getPrescriptionWithId(id), PrescriptionViewModel.class);

        String patient =  prescription.getPrescribeTo().getSalutation() + " " + prescription.getPrescribeTo().getFirstName()
                + " " + prescription.getPrescribeTo().getLastName();

        model.addAttribute("id", id);
        model.addAttribute("patient", patient);
        model.addAttribute("prescription", prescription);

        return "doctors/edit-prescription";

    }

    @PostMapping("/doctor/editPrescription")
    public String postEditPrescription(@ModelAttribute("prescriptionEditBindingModel")
                                                   PrescriptionEditBindingModel prescriptionEditBindingModel,
                                       RedirectAttributes redirectAttributes) {

        this.prescriptionService.editPrescription(prescriptionEditBindingModel);

        redirectAttributes.addAttribute("id", prescriptionEditBindingModel.getPrescriptionId());

        return "redirect:/doctor/prescription";
    }


}