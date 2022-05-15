package com.example.proekt;

import com.example.proekt.service.AppointmentService;
import com.example.proekt.service.DepartmentService;
import com.example.proekt.service.StatusService;
import com.example.proekt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInit implements CommandLineRunner {

    private final UserService userService;
    private final StatusService statusService;
    private final DepartmentService departmentService;
    private final AppointmentService appointmentService;

    @Autowired
    public ApplicationInit(UserService userService, StatusService statusService, DepartmentService departmentService, AppointmentService appointmentService) {
        this.userService = userService;
        this.statusService = statusService;
        this.departmentService = departmentService;
        this.appointmentService = appointmentService;
    }

    @Override
    public void run(String... args) throws Exception {
        this.userService.initUsers();
        this.statusService.initStatuses();
        this.departmentService.initDepartments();
        this.appointmentService.initAppointments();
    }
}
