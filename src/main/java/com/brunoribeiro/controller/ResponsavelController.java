package com.brunoribeiro.controller;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.entities.Responsavel;
import com.brunoribeiro.service.ResponsavelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public List<Responsavel> findAll(){
        return responsavelService.findAll();
    }

    @GetMapping("/{id}")
    public Responsavel findById(@PathVariable UUID id){
        return responsavelService.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        responsavelService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Responsavel update(@PathVariable UUID id, @RequestBody Responsavel responsavel){
        return responsavelService.update(id,responsavel);
    }

    @PostMapping
    public Responsavel create(@RequestBody Responsavel responsavel){
        return responsavelService.create(responsavel);
    }
}
