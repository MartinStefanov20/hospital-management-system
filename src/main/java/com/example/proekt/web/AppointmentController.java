package com.example.proekt.web;

import com.example.proekt.model.binding.AppointmentConfirmationBindingModel;
import com.example.proekt.model.service.AppointmentServiceModel;
import com.example.proekt.model.service.UserServiceModel;
import com.example.proekt.model.view.AppointmentViewModel;
import com.example.proekt.model.view.DoctorViewModelMakeAppointment;
import com.example.proekt.service.AppointmentService;
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
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public AppointmentController(AppointmentService appointmentService, UserService userService, ModelMapper modelMapper) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/appointments")
    public String getAllAppointmentsForPatient(Model model) {

        String patientName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<AppointmentViewModel> appointments = new ArrayList<>();

        for (AppointmentServiceModel appointmentServiceModel : this.appointmentService.getAppointmentsForUserWithUsername(patientName)) {
            AppointmentViewModel appointmentViewModel = this.modelMapper.map(appointmentServiceModel, AppointmentViewModel.class);
            String date = getString(appointmentViewModel);
            appointmentViewModel.setAppointmentTime(date);
            appointments.add(appointmentViewModel);
        }

        model.addAttribute("appointments", appointments);

        return "appointments";
    }

    @GetMapping("/appointments/make-appointment")
    public String makeAppointment(Model model) {

        List<DoctorViewModelMakeAppointment> doctors = new ArrayList<>();

        for (UserServiceModel doctor : this.userService.getAllDoctors()) {
            doctors.add(this.modelMapper.map(doctor, DoctorViewModelMakeAppointment.class));
        }

        model.addAttribute("doctors", doctors);

        return "make-appointment";
    }

    @PostMapping("/appointments/make-appointment")
    public String makeAppointmentPost(@ModelAttribute("doctor")
                                              String doctor,
                                      RedirectAttributes redirectAttributes) {

        if (doctor.equals("Select Doctor")) {
            redirectAttributes.addFlashAttribute("doctorNotSelected", "missing doctor");
            return "redirect:/appointments/make-appointment";
        }

        String patientName = SecurityContextHolder.getContext().getAuthentication().getName();
        this.appointmentService.createAppointmentRequest(doctor, patientName);

        return "redirect:/appointments";
    }


    @GetMapping("/doctor/appointments")
    public String getDoctorAppointments(Model model) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String greeting = this.userService.getDoctorAppointmentsGreeting(username);

        model.addAttribute("greeting", greeting);

        return "doctors/doctors-appointments";
    }

    @GetMapping("/doctor/appointments/requested")
    public String getDoctorRequestedAppointments(Model model) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        List<AppointmentViewModel> appointments = new ArrayList<>();

        for (AppointmentServiceModel appointmentServiceModel : this.appointmentService.getAllRequestedAppointmentsByDoctor(username)) {
            AppointmentViewModel appointmentViewModel = this.modelMapper.map(appointmentServiceModel, AppointmentViewModel.class);
            String date = getString(appointmentViewModel);
            appointmentViewModel.setAppointmentTime(date);
            appointments.add(appointmentViewModel);
        }

        model.addAttribute("appointments", appointments);

        return "doctors/requested-appointments";
    }

    @GetMapping("/doctor/appointments/confirmed")
    public String getDoctorConfirmedAppointments(Model model) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<AppointmentViewModel> appointments = new ArrayList<>();

        for (AppointmentServiceModel appointmentServiceModel : this.appointmentService.getAllConfirmedAppointmentsByDoctor(username)) {
            AppointmentViewModel appointmentViewModel = this.modelMapper.map(appointmentServiceModel, AppointmentViewModel.class);
            String date = getString(appointmentViewModel);
            appointmentViewModel.setAppointmentTime(date);
            appointments.add(appointmentViewModel);
        }

        model.addAttribute("appointments", appointments);

        return "doctors/confirmed-appointments";
    }

    @GetMapping("/doctor/appointments/archived")
    public String getDoctorArchivedAppointments(Model model) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<AppointmentViewModel> appointments = new ArrayList<>();

        for (AppointmentServiceModel appointmentServiceModel : this.appointmentService.getAllArchivedAppointmentsByDoctor(username)) {
            AppointmentViewModel appointmentViewModel = this.modelMapper.map(appointmentServiceModel, AppointmentViewModel.class);
            String date = getString(appointmentViewModel);
            appointmentViewModel.setAppointmentTime(date);
            appointments.add(appointmentViewModel);
        }

        model.addAttribute("appointments", appointments);

        return "doctors/archived-appointments";
    }


    @RequestMapping("/doctor/confirmAppointment")
    public String getConfirmAppointment(@RequestParam("id") Long id, Model model) {

        model.addAttribute("id", id);

        return "doctors/confirm-appointment";
    }

    @PostMapping("/doctor/confirmAppointment")
    public String postConfirmAppointment(@ModelAttribute("appointmentConfirmationBindingModel")
                                                     AppointmentConfirmationBindingModel appointmentConfirmationBindingModel,
                                         RedirectAttributes redirectAttributes) {

        if (appointmentConfirmationBindingModel.getDateAndTime().isEmpty()) {
            redirectAttributes.addFlashAttribute("missingDateTime", "date not selected");
            redirectAttributes.addAttribute("id", appointmentConfirmationBindingModel.getId());
            return "redirect:/doctor/confirmAppointment";
        }

        this.appointmentService
                .confirmAppointment(appointmentConfirmationBindingModel.getId(), appointmentConfirmationBindingModel.getDateAndTime());

        return "redirect:/doctor/appointments/requested";
    }

    @RequestMapping("/doctor/archiveAppointment")
    public String postArchiveAppointment(@RequestParam("id")
                                                 Long id) {

        this.appointmentService.archiveAppointment(id);

        return "redirect:/doctor/appointments/archived";
    }


    private String getString(AppointmentViewModel appointmentViewModel) {

        if (appointmentViewModel.getAppointmentTime() == null) {
            return "TO BE CONFIRMED";
        }

        String date = appointmentViewModel.getAppointmentTime();
        String[] tokens = date.split("T");
        tokens[1] = tokens[1].substring(0, 5);
        date = tokens[0] + " Time: " + tokens[1];

        return date;
    }
}
