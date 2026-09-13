package dev.mstefanov.hms.service.impl;

import dev.mstefanov.hms.exception.NotFoundException;
import dev.mstefanov.hms.model.service.DepartmentServiceModel;
import dev.mstefanov.hms.model.view.DepartmentViewModel;
import dev.mstefanov.hms.repository.DepartmentRepository;
import dev.mstefanov.hms.service.DepartmentService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, ModelMapper modelMapper) {
        this.departmentRepository = departmentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<DepartmentViewModel> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(department -> modelMapper.map(department, DepartmentViewModel.class))
                .toList();
    }

    @Override
    public List<DepartmentServiceModel> getAllDepartmentDetails() {
        return departmentRepository.findAll().stream()
                .map(department -> modelMapper.map(department, DepartmentServiceModel.class))
                .toList();
    }

    @Override
    public DepartmentServiceModel getDepartmentByName(String name) {
        return departmentRepository.findByName(name)
                .map(department -> modelMapper.map(department, DepartmentServiceModel.class))
                .orElseThrow(() -> new NotFoundException("Department '" + name + "' not found"));
    }
}
