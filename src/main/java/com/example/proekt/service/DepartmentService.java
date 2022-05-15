package com.example.proekt.service;

import com.example.proekt.model.service.DepartmentServiceModel;
import com.example.proekt.model.view.DepartmentViewModel;

import java.util.List;

public interface DepartmentService {

    List<DepartmentViewModel> getAllDepartments();

    DepartmentServiceModel getDepartmentByName(String name);

    void initDepartments();

}