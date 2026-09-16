package com.tagox.flow.controller;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantService;
import com.tagox.flow.dto.tenant.TenantRequestDTO;
import com.tagox.flow.dto.tenant.TenantResponseDTO;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<TenantResponseDTO> criar(
            @Valid @RequestBody TenantRequestDTO request
    ) {

        Tenant tenant = tenantService.criar(
                request.tipoPessoa(),
                request.documento(),
                request.nome(),
                request.nomeFantasia(),
                request.slug()
        );

        TenantResponseDTO response = new TenantResponseDTO(
                tenant.getId(),
                tenant.getNome(),
                tenant.getNomeFantasia(),
                tenant.getSlug(),
                tenant.getStatus().name()
        );

        return ResponseEntity.ok(response);
    }
}