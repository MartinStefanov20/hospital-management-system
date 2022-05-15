package com.example.proekt.repository;

import com.example.proekt.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByPatientUsername(String patientUsername);

    List<Appointment> findAllByDoctorUsernameAndStatusName(String username, String statusName);

    Appointment findOneById(Long id);
}
