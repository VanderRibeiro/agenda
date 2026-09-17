package com.brunoribeiro.service;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.repositories.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AlunoService {

    @Autowired
    private AlunoRepository alunoRepository;

    public List<Aluno> findAll() {
        return alunoRepository.findAll();
    }

    public Aluno findById(UUID id) {
        return alunoRepository.findById(id).orElse(null);
    }

    public Aluno update(UUID id, Aluno obj) {
        Aluno entity = alunoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
        updateAluno(entity, obj);
        return alunoRepository.save(entity);
    }

    public void deleteById(UUID id) {
        if(!alunoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado");
        }
        alunoRepository.deleteById(id);
    }

    public void updateAluno(Aluno aluno, Aluno obj){
        aluno.setNome(obj.getNome());
    }
}
