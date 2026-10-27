package com.vinay.dhanpravah.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    @GetMapping("/api/health")
    public String health() {
        return "PayFlow backend is running";
    }

}
