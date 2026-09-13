package dev.mstefanov.hms.api.v1.dto;

import dev.mstefanov.hms.model.service.UserServiceModel;
import dev.mstefanov.hms.model.view.AppointmentUserViewModel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Minimal public view of a user (never exposes credentials or roles)")
public record UserSummary(
        @Schema(example = "3") Long id,
        @Schema(example = "dr.house") String username,
        @Schema(example = "Dr.") String salutation,
        @Schema(example = "Gregory") String firstName,
        @Schema(example = "House") String lastName) {

    public static UserSummary from(AppointmentUserViewModel user) {
        return user == null ? null
                : new UserSummary(user.getId(), user.getUsername(), user.getSalutation(), user.getFirstName(), user.getLastName());
    }

    public static UserSummary from(UserServiceModel user) {
        return user == null ? null
                : new UserSummary(user.getId(), user.getUsername(), user.getSalutation(), user.getFirstName(), user.getLastName());
    }
}
