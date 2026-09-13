package dev.mstefanov.hms.web;

import dev.mstefanov.hms.model.view.DepartmentViewModel;
import dev.mstefanov.hms.service.DepartmentService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DepartmentController {

    private final DepartmentService departmentService;
    private final ModelMapper modelMapper;

    public DepartmentController(DepartmentService departmentService, ModelMapper modelMapper) {
        this.departmentService = departmentService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/departments")
    public String getAllDepartments (Model model) {

        model.addAttribute("departments", this.departmentService.getAllDepartments());

        return "departments";
    }

    @GetMapping("/department")
    public String getDepartment (@RequestParam("name") String name, Model model) {

        DepartmentViewModel department = this.modelMapper
                .map(this.departmentService.getDepartmentByName(name),
                        DepartmentViewModel.class);

        model.addAttribute("department", department);

        return "department";
    }

    @GetMapping("/department/team")
    public String getDepartmentDoctors (@RequestParam("name") String name, Model model) {

        DepartmentViewModel  department =
                this.modelMapper.map(this.departmentService.getDepartmentByName(name), DepartmentViewModel.class);

        model.addAttribute("name", name);
        model.addAttribute("doctors", department.getDoctors());

        return "doctors-by-department";
    }
}
