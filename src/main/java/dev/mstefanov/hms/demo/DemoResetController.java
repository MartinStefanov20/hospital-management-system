package dev.mstefanov.hms.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * {@code POST /internal/demo/reset} wipes and re-seeds the demo data. Protected by a shared secret in the
 * {@code X-Reset-Token} header ({@code app.demo.reset-token}); the endpoint reports 404 while the token is blank.
 */
@RestController
public class DemoResetController {

    static final String TOKEN_HEADER = "X-Reset-Token";

    private final DemoDataService demoDataService;
    private final DemoProperties demoProperties;

    public DemoResetController(DemoDataService demoDataService, DemoProperties demoProperties) {
        this.demoDataService = demoDataService;
        this.demoProperties = demoProperties;
    }

    @PostMapping("/internal/demo/reset")
    public ResponseEntity<Void> reset(@RequestHeader(name = TOKEN_HEADER, required = false) String token) {
        String expected = demoProperties.getResetToken();
        if (expected == null || expected.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (token == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        demoDataService.reset();
        return ResponseEntity.noContent().build();
    }
}
