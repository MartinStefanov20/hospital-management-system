package com.example.proekt.repository;

import com.example.proekt.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findAllByPrescribeToUsername(String username);

    List<Prescription> findAllByPrescribedByUsername(String username);

    Prescription findOneById(Long id);

}
