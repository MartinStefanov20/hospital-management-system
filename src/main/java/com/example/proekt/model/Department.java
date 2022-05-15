package com.example.proekt.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Entity

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Department extends BaseEntity{

    @NotNull
    @Size(max = 45)
    private String name;

    @NotNull
    @Column(length = 4500)
    private String description;

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "departments_doctors",
            joinColumns = @JoinColumn(name="department_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="doctor_id", referencedColumnName = "id"))
    private List<User> doctors;

}
