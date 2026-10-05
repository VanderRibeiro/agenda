package com.brunoribeiro.controller;

import com.brunoribeiro.dto.request.AlunoRequestDTO;
import com.brunoribeiro.dto.response.AlunoResponseDTO;
import com.brunoribeiro.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping
    public List<AlunoResponseDTO> findAll() {
        return alunoService.findAll().stream()
                .map(AlunoResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public AlunoResponseDTO findById(@PathVariable UUID id) {
        return AlunoResponseDTO.fromEntity(alunoService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        alunoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public AlunoResponseDTO update(@PathVariable UUID id, @RequestBody @Valid AlunoRequestDTO alunoRequestDTO) {
        return AlunoResponseDTO.fromEntity(alunoService.update(id, alunoRequestDTO.toEntity()));
    }

    @PostMapping
    public ResponseEntity<AlunoResponseDTO> create(@RequestBody @Valid AlunoRequestDTO alunoRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AlunoResponseDTO.fromEntity(alunoService.create(alunoRequestDTO.toEntity())));
    }
}
