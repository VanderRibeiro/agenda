package com.brunoribeiro.controller;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.entities.Aula;
import com.brunoribeiro.service.AulaService;
import com.brunoribeiro.service.ResponsavelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public List<Aula> findAll(){
        return aulaService.findAll();
    }

    @GetMapping("/{id}")
    public Aula findById(@PathVariable UUID id){
        return aulaService.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        aulaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Aula update(@PathVariable UUID id, @RequestBody Aula aula){
        return aulaService.update(id,aula);
    }

    @PostMapping
    public Aula create(@RequestBody Aula aula){
        return aulaService.create(aula);
    }
}
