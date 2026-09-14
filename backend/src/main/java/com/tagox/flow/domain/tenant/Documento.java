package com.tagox.flow.domain.tenant;

import com.tagox.flow.exception.BusinessException;

import java.util.Objects;

public final class Documento {

    private final TipoPessoa tipoPessoa;
    private final String valor;

    private Documento(TipoPessoa tipoPessoa, String valor) {
        this.tipoPessoa = Objects.requireNonNull(tipoPessoa, "Tipo de pessoa é obrigatório.");
        this.valor = Objects.requireNonNull(valor, "Documento é obrigatório.");
    }

    public static Documento criar(TipoPessoa tipoPessoa, String valor) {
        String valorNormalizado = valor.replaceAll("\\D", "");

        validarTamanho(tipoPessoa, valorNormalizado);
        validarDigitosVerificadores(tipoPessoa, valorNormalizado);
        return new Documento(tipoPessoa, valorNormalizado);
    }

    private static void validarDigitosVerificadores(TipoPessoa tipoPessoa, String valor) {
        if (tipoPessoa == TipoPessoa.FISICA) {
            validarCpf(valor);
        } else {
            validarCnpj(valor);
        }
    }

    private static void validarCpf(String valor) {
        int primeiroDigito = calcularDigitoCpf(valor.substring(0, 9));
        int segundoDigito = calcularDigitoCpf(valor.substring(0, 10));

        if (primeiroDigito != Character.getNumericValue(valor.charAt(9))
                || segundoDigito != Character.getNumericValue(valor.charAt(10))) {
            throw new BusinessException("CPF inválido.");
        }
    }

    private static int calcularDigitoCpf(String valor) {
        int soma = 0;
        int peso = valor.length() + 1;

        for (char caractere : valor.toCharArray()) {
            soma += Character.getNumericValue(caractere) * peso;
            peso--;
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }


    private static void validarCnpj(String valor) {
        int primeiroDigito = calcularDigitoCnpj(valor.substring(0, 12));
        int segundoDigito = calcularDigitoCnpj(valor.substring(0, 13));

        if (primeiroDigito != Character.getNumericValue(valor.charAt(12))
                || segundoDigito != Character.getNumericValue(valor.charAt(13))) {
            throw new BusinessException("CNPJ inválido.");
        }
    }

    private static int calcularDigitoCnpj(String valor) {
        int[] pesos = valor.length() == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma = 0;

        for (int i = 0; i < valor.length(); i++) {
            soma += Character.getNumericValue(valor.charAt(i)) * pesos[i];
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }


    private static void validarTamanho(TipoPessoa tipoPessoa, String valor) {
        int tamanhoEsperado = tipoPessoa == TipoPessoa.FISICA ? 11 : 14;

        if (valor.length() != tamanhoEsperado) {
            throw new BusinessException(
                    "Documento inválido para o tipo de pessoa informado."
            );
        }
    }


    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Documento documento)) {
            return false;
        }

        return tipoPessoa == documento.tipoPessoa
                && valor.equals(documento.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoPessoa, valor);
    }
}
