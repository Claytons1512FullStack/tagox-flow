package com.tagox.flow.domain.tenant;

import com.tagox.flow.exception.DuplicateResourceException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class TenantService {


    private final TenantRepository tenantRepository;


    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }



    @Transactional
    public Tenant criar(
            TipoPessoa tipoPessoa,
            String documento,
            String nome,
            String nomeFantasia,
            String slug
    ) {
        Documento documentoNormalizado = Documento.criar(tipoPessoa, documento);
        String valorDocumento = documentoNormalizado.getValor();



        if (tenantRepository.existsByDocumento(valorDocumento)) {

            throw new DuplicateResourceException(
                    "Documento já cadastrado."
            );

        }



        if (tenantRepository.existsBySlug(slug)) {

            throw new DuplicateResourceException(
                    "Slug já cadastrado."
            );

        }



        Tenant tenant = new Tenant(
                tipoPessoa,
                valorDocumento,
                nome,
                nomeFantasia,
                slug,
                TenantStatus.TRIAL
        );


        return tenantRepository.save(tenant);

    }

}