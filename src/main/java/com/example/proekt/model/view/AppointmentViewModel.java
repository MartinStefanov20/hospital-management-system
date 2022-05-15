package com.example.proekt.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentViewModel {

    private Long id;
    private AppointmentUserViewModel doctor;
    private AppointmentUserViewModel patient;
    private String appointmentTime;
    private StatusViewModel status;

}