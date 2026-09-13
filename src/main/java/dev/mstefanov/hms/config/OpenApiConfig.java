package dev.mstefanov.hms.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for the {@code /api/v1} REST API. The generated document is served at {@code /v3/api-docs}
 * and Swagger UI at {@code /swagger-ui.html}; every operation requires HTTP Basic credentials.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Hospital Management System API",
                version = "v1",
                description = """
                        REST API of the Hospital Management System demo. Patients request appointments, doctors
                        confirm or archive them and issue prescriptions, admins see everything.

                        Authenticate with HTTP Basic. Demo accounts: `patient/patient123`, `dr.house/doctor123`,
                        `admin/admin123`.""",
                contact = @Contact(name = "Martin Stefanov", url = "https://github.com/MartinStefanov20"),
                license = @License(name = "MIT")),
        security = @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH))
@SecurityScheme(
        name = OpenApiConfig.BASIC_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "HTTP Basic with the application credentials")
public class OpenApiConfig {

    public static final String BASIC_AUTH = "basicAuth";
}
