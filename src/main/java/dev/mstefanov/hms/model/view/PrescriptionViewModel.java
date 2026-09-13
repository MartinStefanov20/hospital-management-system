package dev.mstefanov.hms.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrescriptionViewModel {

    private Long id;
    private AppointmentUserViewModel prescribedBy;
    private AppointmentUserViewModel prescribeTo;
    private String date;
    private String prescriptionNotes;


}
