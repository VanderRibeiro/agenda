package com.brunoribeiro.service;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.entities.Aula;
import com.brunoribeiro.repositories.AlunoRepository;
import com.brunoribeiro.repositories.AulaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AulaService {

    @Autowired
    private AulaRepository aulaRepository;

    public List<Aula> findAll() {
        return aulaRepository.findAll();
    }

    public Aula findById(UUID id) {
        return aulaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada"));
    }

    public Aula update(UUID id, Aula obj) {
        Aula entity = aulaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada"));
        updateAula(entity, obj);
        return aulaRepository.save(entity);
    }

    public void deleteById(UUID id) {
        if(!aulaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada");
        }
        aulaRepository.deleteById(id);
    }

    public void updateAula(Aula aula, Aula obj){
        aula.setData(obj.getData());
    }

    public Aula create(Aula aula) {
        return aulaRepository.save(aula);
    }
}
