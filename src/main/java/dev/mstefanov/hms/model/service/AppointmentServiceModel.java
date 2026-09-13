package dev.mstefanov.hms.model.service;

import dev.mstefanov.hms.model.view.AppointmentUserViewModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentServiceModel extends BaseServiceModel {

    private AppointmentUserViewModel doctor;
    private AppointmentUserViewModel patient;
    private LocalDateTime appointmentTime;
    private StatusServiceModel status;

}

