package com.brunoribeiro.controller;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.entities.Professor;
import com.brunoribeiro.service.ProfessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/professores")
public class ProfessorController {

    @Autowired
    public ProfessorService professorService;

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

    @PutMapping
    public Professor update(@PathVariable UUID id, @RequestBody Professor professor){
        return professorService.update(id,professor);
    }
}
