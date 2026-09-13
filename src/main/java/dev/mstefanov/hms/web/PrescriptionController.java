package dev.mstefanov.hms.web;

import dev.mstefanov.hms.model.binding.PrescriptionBindingModel;
import dev.mstefanov.hms.model.binding.PrescriptionEditBindingModel;
import dev.mstefanov.hms.model.service.PrescriptionServiceModel;
import dev.mstefanov.hms.model.view.PrescriptionViewModel;
import dev.mstefanov.hms.service.AppointmentService;
import dev.mstefanov.hms.service.PrescriptionService;
import dev.mstefanov.hms.service.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final ModelMapper modelMapper;

    public PrescriptionController(PrescriptionService prescriptionService, ModelMapper modelMapper,
                                  UserService userService, AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/prescriptions")
    public String getAllPrescriptionsPerPatient(Model model, Principal principal) {
        model.addAttribute("prescriptions", toViews(prescriptionService.getPrescriptionsForUser(principal.getName())));
        return "prescriptions";
    }

    @RequestMapping("/prescriptions/prescription")
    public String getPatientPrescription(@RequestParam("id") Long id, Model model) {
        model.addAttribute("prescription", toView(prescriptionService.getPrescriptionWithId(id)));
        return "prescription";
    }

    /** Opens the prescription form; the appointment is archived as soon as the doctor starts issuing. */
    @RequestMapping("/doctor/issuePrescription")
    public String archiveAppointmentAndIssuePrescription(@RequestParam("appointmentId") Long appointmentId,
                                                         @RequestParam("patientId") Long patientId,
                                                         Model model) {
        model.addAttribute("appointmentId", appointmentId);
        model.addAttribute("patientId", patientId);
        model.addAttribute("nameOfUser", userService.getUserFullName(patientId));
        appointmentService.archiveAppointment(appointmentId);
        return "doctors/prescription-form";
    }

    @PostMapping("/doctor/issuePrescription")
    public String issuePrescription(@Valid @ModelAttribute("prescriptionBindingModel") PrescriptionBindingModel bindingModel,
                                    BindingResult bindingResult,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("bindingIssue", AppointmentController.firstError(bindingResult));
            redirectAttributes.addAttribute("appointmentId", bindingModel.getAppointmentId());
            redirectAttributes.addAttribute("patientId", bindingModel.getPatientId());
            return "redirect:/doctor/issuePrescription";
        }
        prescriptionService.issuePrescription(principal.getName(), bindingModel.getPatientId(),
                bindingModel.getAppointmentId(), bindingModel.getNotes());
        return "redirect:/doctor/appointments/confirmed";
    }

    @GetMapping("/doctor/prescriptions")
    public String getAllPrescriptionsPerDoctor(Model model, Principal principal) {
        model.addAttribute("prescriptions", toViews(prescriptionService.getPrescriptionsForDoctor(principal.getName())));
        return "doctors/doctors-prescriptions";
    }

    @RequestMapping("/doctor/prescription")
    public String getDoctorPrescription(@RequestParam("id") Long id, Model model) {
        model.addAttribute("prescription", toView(prescriptionService.getPrescriptionWithId(id)));
        return "doctors/doctors-prescription";
    }

    @GetMapping("/doctor/editPrescription")
    public String getEditPrescription(@RequestParam("id") Long id, Model model) {
        PrescriptionViewModel prescription = toView(prescriptionService.getPrescriptionWithId(id));
        String patient = prescription.getPrescribeTo().getSalutation() + " " + prescription.getPrescribeTo().getFirstName()
                + " " + prescription.getPrescribeTo().getLastName();
        model.addAttribute("id", id);
        model.addAttribute("patient", patient);
        model.addAttribute("prescription", prescription);
        return "doctors/edit-prescription";
    }

    @PostMapping("/doctor/editPrescription")
    public String postEditPrescription(@Valid @ModelAttribute("prescriptionEditBindingModel")
                                       PrescriptionEditBindingModel bindingModel,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("bindingIssue", AppointmentController.firstError(bindingResult));
            redirectAttributes.addAttribute("id", bindingModel.getPrescriptionId());
            return "redirect:/doctor/editPrescription";
        }
        prescriptionService.editPrescription(bindingModel.getPrescriptionId(), bindingModel.getNotes());
        redirectAttributes.addAttribute("id", bindingModel.getPrescriptionId());
        return "redirect:/doctor/prescription";
    }

    private List<PrescriptionViewModel> toViews(List<PrescriptionServiceModel> prescriptions) {
        return prescriptions.stream().map(this::toView).toList();
    }

    private PrescriptionViewModel toView(PrescriptionServiceModel prescription) {
        return modelMapper.map(prescription, PrescriptionViewModel.class);
    }
}
