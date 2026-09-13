package dev.mstefanov.hms.api.v1;

import dev.mstefanov.hms.api.v1.dto.DepartmentResponse;
import dev.mstefanov.hms.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/departments", produces = "application/json")
@Tag(name = "Departments", description = "Hospital departments and their doctors")
public class DepartmentApiController {

    private final DepartmentService departmentService;

    public DepartmentApiController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "List all departments")
    public List<DepartmentResponse> list() {
        return departmentService.getAllDepartmentDetails().stream().map(DepartmentResponse::from).toList();
    }

    @GetMapping("/{name}")
    @Operation(summary = "Get a department by name")
    @ApiResponse(responseCode = "200", description = "Department found")
    @ApiResponse(responseCode = "404", description = "No department with that name")
    public DepartmentResponse get(@PathVariable String name) {
        return DepartmentResponse.from(departmentService.getDepartmentByName(name));
    }
}
