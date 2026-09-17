package com.brunoribeiro.controller;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.service.AlunoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @GetMapping
    public List<Aluno> findAll(){
        return alunoService.findAll();
    }

    @GetMapping("/{id}")
    public Aluno findById(@PathVariable UUID id){
        return alunoService.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        alunoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public Aluno update(@PathVariable UUID id, @RequestBody Aluno aluno){
        return alunoService.update(id,aluno);
    }
}
