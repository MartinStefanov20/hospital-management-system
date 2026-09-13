package dev.mstefanov.hms;

import dev.mstefanov.hms.support.IntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The key schema/entity consistency test: the context only starts if Flyway applied every migration and
 * Hibernate's {@code ddl-auto=validate} accepted the resulting schema for all JPA entities.
 */
@IntegrationTest
class HospitalManagementSystemApplicationIT {

    @Autowired
    Flyway flyway;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void contextLoadsAndFlywayIsAtLatestVersion() {
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("3");
    }

    @Test
    void referenceDataAndSchemaArePresent() {
        Integer statuses = jdbcTemplate.queryForObject("select count(*) from status", Integer.class);
        assertThat(statuses).isEqualTo(3);

        Integer nameLength = jdbcTemplate.queryForObject(
                "select character_maximum_length from information_schema.columns "
                        + "where table_name = 'custom_user' and column_name = 'first_name'", Integer.class);
        assertThat(nameLength).isEqualTo(50);
    }
}
