package com.tagox.flow.controller;

import com.tagox.flow.domain.patient.Patient;
import com.tagox.flow.dto.patient.CreatePatientRequest;
import com.tagox.flow.dto.patient.PatientResponse;
import com.tagox.flow.security.AuthenticatedUser;
import com.tagox.flow.service.PatientService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> criarPaciente(
            @Valid @RequestBody CreatePatientRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        Patient patient = patientService.criarPaciente(
                request,
                authenticatedUser.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PatientResponse.from(patient));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> listarPacientes(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        List<PatientResponse> pacientes = patientService
                .listarPacientes(authenticatedUser.getTenantId())
                .stream()
                .map(PatientResponse::from)
                .toList();

        return ResponseEntity.ok(pacientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> buscarPaciente(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        Patient patient = patientService.buscarPaciente(
                id,
                authenticatedUser.getTenantId()
        );

        return ResponseEntity.ok(PatientResponse.from(patient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> atualizarPaciente(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePatientRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        Patient patient = patientService.atualizarPaciente(
                id,
                request,
                authenticatedUser.getTenantId()
        );

        return ResponseEntity.ok(PatientResponse.from(patient));
    }
}
