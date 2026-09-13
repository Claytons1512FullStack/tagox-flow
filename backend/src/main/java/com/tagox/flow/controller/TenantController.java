package com.tagox.flow.controller;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantService;
import com.tagox.flow.dto.tenant.TenantRequestDTO;
import com.tagox.flow.dto.tenant.TenantResponseDTO;
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
            @RequestBody TenantRequestDTO request
    ) {

        Tenant tenant = tenantService.criar(
                request.getTipoPessoa(),
                request.getDocumento(),
                request.getNome(),
                request.getNomeFantasia(),
                request.getSlug()
        );

        return ResponseEntity.ok(
                TenantResponseDTO.from(tenant)
        );
    }
}