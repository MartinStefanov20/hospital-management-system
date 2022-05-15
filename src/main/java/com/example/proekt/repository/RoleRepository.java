package com.example.proekt.repository;

import com.example.proekt.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findAllByUserUsername (String username);

    @Modifying
    @Query("delete from Role r where r.id=:id")
    void deleteOneById(@Param("id") Long id);

}
