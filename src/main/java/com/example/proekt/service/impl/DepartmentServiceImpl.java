package com.example.proekt.service.impl;

import com.example.proekt.model.Department;
import com.example.proekt.model.service.DepartmentServiceModel;
import com.example.proekt.model.view.DepartmentViewModel;
import com.example.proekt.repository.DepartmentRepository;
import com.example.proekt.service.DepartmentService;
import com.example.proekt.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, UserService userService, ModelMapper modelMapper) {
        this.departmentRepository = departmentRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<DepartmentViewModel> getAllDepartments() {

        List<Department> departments = this.departmentRepository.findAll();
        List<DepartmentViewModel> viewDepartments = new ArrayList<>();

        for (Department department : departments) {
            viewDepartments.add(this.modelMapper.
                    map(department, DepartmentViewModel.class));
        }

        return viewDepartments;
    }

    @Override
    public DepartmentServiceModel getDepartmentByName(String name) {

        return this.modelMapper.map
                (this.departmentRepository.findDepartmentByName(name),
                        DepartmentServiceModel.class);

    }

    @Override
    public void initDepartments() {
        if (this.departmentRepository.count() == 0) {

            Department cardiology = new Department( "Cardiology",
                    "Cardiology Description Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,", new ArrayList<>());
            cardiology.getDoctors().add(this.userService.getUserByUsername("martin.doctor"));

            Department neurology = new Department("Neurology",
                    "Neurology Description Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,", new ArrayList<>());
            neurology.getDoctors().add(this.userService.getUserByUsername("martin.doctor"));

            Department orthopedics = new Department("Orthopedics",
                    "Orthopedics Description Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,", new ArrayList<>());
            orthopedics.getDoctors().add(this.userService.getUserByUsername("martin.doctor"));

            Department pediatrics = new Department("Pediatrics",
                    "Pediatrics Description Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,", new ArrayList<>());
            pediatrics.getDoctors().add(this.userService.getUserByUsername("martin.doctor"));

            Department medicalImaging = new Department("Medical Imaging",
                    "Medical Imaging Descriptio Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,n", new ArrayList<>());
            medicalImaging.getDoctors().add(this.userService.getUserByUsername("martin.doctor"));

            this.departmentRepository.saveAll(List.of(cardiology, neurology, orthopedics, pediatrics, medicalImaging));
        }
    }
}
