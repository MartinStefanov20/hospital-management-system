package com.example.proekt.model.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepartmentServiceModel extends BaseServiceModel {

    private String name;
    private String description;
    private List<UserServiceModel> doctors;

}