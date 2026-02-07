package com.tejas.splitspend.health;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-check")
public class HealthController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getHealthStatus() {
        return  ResponseEntity.status(HttpStatus.OK).body("The application is well and healthy... (for now!)");
    }
}
