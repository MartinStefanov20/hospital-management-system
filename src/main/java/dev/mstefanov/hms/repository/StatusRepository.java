package dev.mstefanov.hms.repository;

import dev.mstefanov.hms.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

    Status findStatusByName (String name);

}
