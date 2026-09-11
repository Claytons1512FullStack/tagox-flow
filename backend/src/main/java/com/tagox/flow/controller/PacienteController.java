package com.tagox.flow.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tagox.flow.dto.PacienteRequestDTO;
import com.tagox.flow.dto.PacienteResponseDTO;
import com.tagox.flow.service.PacienteService;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    // Injeção de dependência via construtor
    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    // Endpoint para cadastrar um novo paciente
    @PostMapping
    public ResponseEntity<PacienteResponseDTO> criar(@Valid @RequestBody PacienteRequestDTO dto) {
        PacienteResponseDTO pacienteCriado = pacienteService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteCriado);
    }

    // Endpoint para listar todos os pacientes
    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> listarTodos() {
        List<PacienteResponseDTO> pacientes = pacienteService.listarTodos();
        return ResponseEntity.ok(pacientes);
    }

    // Endpoint para buscar um paciente por ID
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> buscarPorId(@PathVariable Long id) {
        PacienteResponseDTO paciente = pacienteService.buscarPorId(id);
        return ResponseEntity.ok(paciente);
    }
    // Endpoint para atualizar um paciente
    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequestDTO dto) {
        PacienteResponseDTO pacienteAtualizado = pacienteService.atualizar(id, dto);
        return ResponseEntity.ok(pacienteAtualizado);
    }

    // Endpoint para deletar um paciente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pacienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}