package com.palettee;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health-check")
    public HealthDto healthCheck() {
        return new HealthDto("up");
    }

}
