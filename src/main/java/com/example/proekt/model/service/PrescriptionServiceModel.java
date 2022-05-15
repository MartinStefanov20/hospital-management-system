package com.example.proekt.model.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrescriptionServiceModel extends BaseServiceModel{

    private UserServiceModel prescribedBy;
    private UserServiceModel prescribeTo;
    private String date;
    private String prescriptionNotes;

}
