package dev.mstefanov.hms.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Seeds demo data on startup when the {@code demo} profile is active and the database has no users. */
@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {

    private final DemoDataService demoDataService;

    public DemoDataInitializer(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @Override
    public void run(String... args) {
        demoDataService.seedIfEmpty();
    }
}
