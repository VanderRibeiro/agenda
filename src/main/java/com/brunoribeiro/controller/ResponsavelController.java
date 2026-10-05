package com.brunoribeiro.controller;

import com.brunoribeiro.dto.request.ResponsavelRequestDTO;
import com.brunoribeiro.dto.response.ResponsavelResponseDTO;
import com.brunoribeiro.service.ResponsavelService;
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
@RequestMapping("/responsaveis")
public class ResponsavelController {

    private final ResponsavelService responsavelService;

    public ResponsavelController(ResponsavelService responsavelService) {
        this.responsavelService = responsavelService;
    }

    @GetMapping
    public List<ResponsavelResponseDTO> findAll() {
        return responsavelService.findAll().stream()
                .map(ResponsavelResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponsavelResponseDTO findById(@PathVariable UUID id) {
        return ResponsavelResponseDTO.fromEntity(responsavelService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        responsavelService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponsavelResponseDTO update(@PathVariable UUID id, @RequestBody @Valid ResponsavelRequestDTO responsavelRequestDTO) {
        return ResponsavelResponseDTO.fromEntity(responsavelService.update(id, responsavelRequestDTO.toEntity()));
    }

    @PostMapping
    public ResponsavelResponseDTO create(@RequestBody @Valid ResponsavelRequestDTO responsavelRequestDTO) {
        return ResponsavelResponseDTO.fromEntity(responsavelService.create(responsavelRequestDTO.toEntity()));
    }
}
