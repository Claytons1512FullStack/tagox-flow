package com.tagox.flow.controller;

import com.tagox.flow.dto.onboarding.OnboardingRequest;
import com.tagox.flow.dto.onboarding.OnboardingResponse;
import com.tagox.flow.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping
    public ResponseEntity<OnboardingResponse> cadastrar(
            @Valid @RequestBody OnboardingRequest request
    ) {
        OnboardingResponse response = onboardingService.cadastrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
