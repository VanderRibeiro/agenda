package com.brunoribeiro.controller;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.entities.Professor;
import com.brunoribeiro.service.ProfessorService;
import com.brunoribeiro.service.ResponsavelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public List<Professor> findAll(){
        return professorService.findAll();
    }

    @GetMapping("/{id}")
    public Professor findById(@PathVariable UUID id){
        return professorService.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        professorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Professor update(@PathVariable UUID id, @RequestBody Professor professor){
        return professorService.update(id,professor);
    }

    @PostMapping
    public Professor create(@RequestBody Professor professor){
        return professorService.create(professor);
    }
}
