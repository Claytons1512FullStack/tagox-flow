package com.tagox.flow.dto.agenda;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class CriarAgendamentoPublicoRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    private String nome;

    @Size(max = 14, message = "O CPF deve ter no máximo 14 caracteres")
    @Pattern(
            regexp = "^[0-9./ -]*$",
            message = "O CPF contém caracteres inválidos"
    )
    private String cpf;

    @PastOrPresent(message = "A data de nascimento não pode ser futura")
    private LocalDate dataNascimento;

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Email(message = "O e-mail informado é inválido")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    private String email;

    @NotNull(message = "A data da consulta é obrigatória")
    private LocalDate data;

    @NotNull(message = "O horário de início é obrigatório")
    private LocalTime horarioInicio;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres")
    private String observacoes;

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}