package dev.mstefanov.hms.model.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrescriptionServiceModel extends BaseServiceModel {

    private UserServiceModel prescribedBy;
    private UserServiceModel prescribeTo;
    private LocalDate date;
    private String prescriptionNotes;
    private Long appointmentId;
}
