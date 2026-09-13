package dev.mstefanov.hms.service;

import dev.mstefanov.hms.model.service.DepartmentServiceModel;
import dev.mstefanov.hms.model.view.DepartmentViewModel;

import java.util.List;

public interface DepartmentService {

    List<DepartmentViewModel> getAllDepartments();

    List<DepartmentServiceModel> getAllDepartmentDetails();

    DepartmentServiceModel getDepartmentByName(String name);
}
