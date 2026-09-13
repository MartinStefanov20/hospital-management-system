package dev.mstefanov.hms.demo;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/** Exposes the demo accounts to views (the login page shows them) while the {@code demo} profile is active. */
@ControllerAdvice
@Profile("demo")
public class DemoLoginHintAdvice {

    private final DemoProperties demoProperties;

    public DemoLoginHintAdvice(DemoProperties demoProperties) {
        this.demoProperties = demoProperties;
    }

    @ModelAttribute("demoAccounts")
    public List<DemoProperties.Account> demoAccounts() {
        return demoProperties.getAccounts();
    }
}
