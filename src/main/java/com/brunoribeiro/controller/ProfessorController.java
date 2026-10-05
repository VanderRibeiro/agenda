package com.brunoribeiro.controller;

import com.brunoribeiro.dto.request.ProfessorRequestDTO;
import com.brunoribeiro.dto.response.ProfessorResponseDTO;
import com.brunoribeiro.service.ProfessorService;
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
@RequestMapping("/professores")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping
    public List<ProfessorResponseDTO> findAll() {
        return professorService.findAll().stream()
                .map(ProfessorResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ProfessorResponseDTO findById(@PathVariable UUID id) {
        return ProfessorResponseDTO.fromEntity(professorService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        professorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ProfessorResponseDTO update(@PathVariable UUID id, @RequestBody @Valid ProfessorRequestDTO professorRequestDTO) {
        return ProfessorResponseDTO.fromEntity(professorService.update(id, professorRequestDTO.toEntity()));
    }

    @PostMapping
    public ResponseEntity<ProfessorResponseDTO> create(@RequestBody @Valid ProfessorRequestDTO professorRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProfessorResponseDTO.fromEntity(professorService.create(professorRequestDTO.toEntity())));
    }
}
