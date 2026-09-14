package com.tagox.flow.domain.tenant;

import com.tagox.flow.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentoTest {

    @Test
    void deveNormalizarCpfRemovendoFormatacao() {
        Documento documento = Documento.criar(
                TipoPessoa.FISICA,
                "529.982.247-25"
        );

        assertEquals("52998224725", documento.getValor());
    }

    @Test
    void deveNormalizarCnpjRemovendoFormatacao() {
        Documento documento = Documento.criar(
                TipoPessoa.JURIDICA,
                "11.222.333/0001-81"
        );

        assertEquals("11222333000181", documento.getValor());
    }

    @Test
    void deveRejeitarCpfComTamanhoInvalido() {
        assertThrows(
                BusinessException.class,
                () -> Documento.criar(TipoPessoa.FISICA, "1234567890")
        );
    }

    @Test
    void deveRejeitarCnpjComTamanhoInvalido() {
        assertThrows(
                BusinessException.class,
                () -> Documento.criar(TipoPessoa.JURIDICA, "1234567890123")
        );
    }

    @Test
    void deveRejeitarCpfComDigitosVerificadoresInvalidos() {
        assertThrows(
                BusinessException.class,
                () -> Documento.criar(TipoPessoa.FISICA, "52998224726")
        );
    }

    @Test
    void deveRejeitarCnpjComDigitosVerificadoresInvalidos() {
        assertThrows(
                BusinessException.class,
                () -> Documento.criar(TipoPessoa.JURIDICA, "11222333000182")
        );
    }
}
