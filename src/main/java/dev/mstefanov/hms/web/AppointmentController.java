package dev.mstefanov.hms.web;

import dev.mstefanov.hms.model.binding.AppointmentConfirmationBindingModel;
import dev.mstefanov.hms.model.service.AppointmentServiceModel;
import dev.mstefanov.hms.model.service.UserServiceModel;
import dev.mstefanov.hms.model.view.AppointmentViewModel;
import dev.mstefanov.hms.model.view.DoctorViewModelMakeAppointment;
import dev.mstefanov.hms.service.AppointmentService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class AppointmentController {

    private static final DateTimeFormatter VIEW_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd 'Time:' HH:mm");

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public AppointmentController(AppointmentService appointmentService, UserService userService, ModelMapper modelMapper) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/appointments")
    public String getAllAppointmentsForPatient(Model model, Principal principal) {
        model.addAttribute("appointments", toViews(appointmentService.getAppointmentsForUserWithUsername(principal.getName())));
        return "appointments";
    }

    @GetMapping("/appointments/make-appointment")
    public String makeAppointment(Model model) {
        List<DoctorViewModelMakeAppointment> doctors = userService.getAllDoctors().stream()
                .map(doctor -> modelMapper.map(doctor, DoctorViewModelMakeAppointment.class))
                .toList();
        model.addAttribute("doctors", doctors);
        return "make-appointment";
    }

    @PostMapping("/appointments/make-appointment")
    public String makeAppointmentPost(@ModelAttribute("doctor") String doctor,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (doctor == null || doctor.isBlank() || doctor.equals("Select Doctor")) {
            redirectAttributes.addFlashAttribute("doctorNotSelected", "missing doctor");
            return "redirect:/appointments/make-appointment";
        }
        appointmentService.createAppointmentRequest(doctor, principal.getName());
        return "redirect:/appointments";
    }

    @GetMapping("/doctor/appointments")
    public String getDoctorAppointments(Model model, Principal principal) {
        model.addAttribute("greeting", userService.getDoctorAppointmentsGreeting(principal.getName()));
        return "doctors/doctors-appointments";
    }

    @GetMapping("/doctor/appointments/requested")
    public String getDoctorRequestedAppointments(Model model, Principal principal) {
        model.addAttribute("appointments", toViews(appointmentService.getAllRequestedAppointmentsByDoctor(principal.getName())));
        return "doctors/requested-appointments";
    }

    @GetMapping("/doctor/appointments/confirmed")
    public String getDoctorConfirmedAppointments(Model model, Principal principal) {
        model.addAttribute("appointments", toViews(appointmentService.getAllConfirmedAppointmentsByDoctor(principal.getName())));
        return "doctors/confirmed-appointments";
    }

    @GetMapping("/doctor/appointments/archived")
    public String getDoctorArchivedAppointments(Model model, Principal principal) {
        model.addAttribute("appointments", toViews(appointmentService.getAllArchivedAppointmentsByDoctor(principal.getName())));
        return "doctors/archived-appointments";
    }

    @GetMapping("/doctor/confirmAppointment")
    public String getConfirmAppointment(@RequestParam("id") Long id, Model model) {
        model.addAttribute("id", id);
        return "doctors/confirm-appointment";
    }

    @PostMapping("/doctor/confirmAppointment")
    public String postConfirmAppointment(@Valid @ModelAttribute("appointmentConfirmationBindingModel")
                                         AppointmentConfirmationBindingModel bindingModel,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("missingDateTime", firstError(bindingResult));
            redirectAttributes.addAttribute("id", bindingModel.getId());
            return "redirect:/doctor/confirmAppointment";
        }
        appointmentService.confirmAppointment(bindingModel.getId(), bindingModel.getDateAndTime());
        return "redirect:/doctor/appointments/requested";
    }

    @RequestMapping("/doctor/archiveAppointment")
    public String postArchiveAppointment(@RequestParam("id") Long id) {
        appointmentService.archiveAppointment(id);
        return "redirect:/doctor/appointments/archived";
    }

    static String firstError(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid input");
    }

    private List<AppointmentViewModel> toViews(List<AppointmentServiceModel> appointments) {
        return appointments.stream().map(this::toView).toList();
    }

    private AppointmentViewModel toView(AppointmentServiceModel appointment) {
        AppointmentViewModel view = modelMapper.map(appointment, AppointmentViewModel.class);
        view.setAppointmentTime(appointment.getAppointmentTime() == null
                ? "TO BE CONFIRMED"
                : appointment.getAppointmentTime().format(VIEW_FORMAT));
        return view;
    }
}
