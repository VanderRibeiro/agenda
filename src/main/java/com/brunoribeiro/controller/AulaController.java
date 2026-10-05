package com.brunoribeiro.controller;

import com.brunoribeiro.dto.request.AulaRequestDTO;
import com.brunoribeiro.dto.response.AulaResponseDTO;
import com.brunoribeiro.service.AulaService;
import jakarta.validation.Valid;
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
@RequestMapping("/aulas")
public class AulaController {

    private final AulaService aulaService;

    public AulaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    @GetMapping
    public List<AulaResponseDTO> findAll() {
        return aulaService.findAll().stream()
                .map(AulaResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public AulaResponseDTO findById(@PathVariable UUID id) {
        return AulaResponseDTO.fromEntity(aulaService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        aulaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public AulaResponseDTO update(@PathVariable UUID id, @RequestBody @Valid AulaRequestDTO aulaRequestDTO) {
        return AulaResponseDTO.fromEntity(aulaService.update(id, aulaRequestDTO.toEntity()));
    }

    @PostMapping
    public AulaResponseDTO create(@RequestBody @Valid AulaRequestDTO aulaRequestDTO) {
        return AulaResponseDTO.fromEntity(aulaService.create(aulaRequestDTO.toEntity()));
    }
}
