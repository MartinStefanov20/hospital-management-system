package dev.mstefanov.hms.api.v1.dto;

import dev.mstefanov.hms.model.service.DepartmentServiceModel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A hospital department and the doctors working in it")
public record DepartmentResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Cardiology") String name,
        String description,
        List<UserSummary> doctors) {

    public static DepartmentResponse from(DepartmentServiceModel department) {
        List<UserSummary> doctors = department.getDoctors() == null ? List.of()
                : department.getDoctors().stream().map(UserSummary::from).toList();
        return new DepartmentResponse(department.getId(), department.getName(), department.getDescription(), doctors);
    }
}
