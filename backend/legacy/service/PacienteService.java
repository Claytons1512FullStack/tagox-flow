package com.tagox.flow.service;

import com.tagox.flow.dto.PacienteRequestDTO;
import com.tagox.flow.dto.PacienteResponseDTO;
import com.tagox.flow.model.Paciente;
import com.tagox.flow.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    // Injeção de dependência via construtor
    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    // Salvar um novo paciente com validações de unicidade
    public PacienteResponseDTO salvar(PacienteRequestDTO dto) {
        if (pacienteRepository.existsByCpf(dto.getCpf())) {
            throw new IllegalArgumentException("Já existe um paciente cadastrado com este CPF.");
        }

        if (pacienteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Já existe um paciente cadastrado com este e-mail.");
        }

        Paciente paciente = new Paciente();
        paciente.setNome(dto.getNome());
        paciente.setCpf(dto.getCpf());
        paciente.setEmail(dto.getEmail());
        paciente.setTelefone(dto.getTelefone());
        paciente.setDataNascimento(dto.getDataNascimento());
        paciente.setObservacoes(dto.getObservacoes());

        Paciente pacienteSalvo = pacienteRepository.save(paciente);
        return new PacienteResponseDTO(pacienteSalvo);
    }

    // Listar todos os pacientes
    public List<PacienteResponseDTO> listarTodos() {
        return pacienteRepository.findAll()
                .stream()
                .map(PacienteResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar paciente por ID
    public PacienteResponseDTO buscarPorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com o ID: " + id));
        return new PacienteResponseDTO(paciente);
    }

    // Atualizar paciente existente com validação de duplicidade ajustada
    public PacienteResponseDTO atualizar(Long id, PacienteRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com o ID: " + id));

        // Validar se o novo CPF pertence a OUTRO paciente diferente do atual
        if (!paciente.getCpf().equalsIgnoreCase(dto.getCpf()) && pacienteRepository.existsByCpf(dto.getCpf())) {
            throw new IllegalArgumentException("Já existe outro paciente cadastrado com este CPF.");
        }

        // Validar se o novo e-mail pertence a OUTRO paciente diferente do atual
        if (!paciente.getEmail().equalsIgnoreCase(dto.getEmail()) && pacienteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Já existe outro paciente cadastrado com este e-mail.");
        }

        paciente.setNome(dto.getNome());
        paciente.setCpf(dto.getCpf());
        paciente.setEmail(dto.getEmail());
        paciente.setTelefone(dto.getTelefone());
        paciente.setDataNascimento(dto.getDataNascimento());
        paciente.setObservacoes(dto.getObservacoes());

        Paciente pacienteAtualizado = pacienteRepository.save(paciente);
        return new PacienteResponseDTO(pacienteAtualizado);
    }

    // Excluir paciente por ID
    public void deletar(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new IllegalArgumentException("Paciente não encontrado com o ID: " + id);
        }
        pacienteRepository.deleteById(id);
    }
}