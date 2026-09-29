package com.brunoribeiro.service;

import com.brunoribeiro.entities.Professor;
import com.brunoribeiro.repositories.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProfessorService {

    @Autowired
    public ProfessorRepository professorRepository;

    public List<Professor> findAll() {
        return professorRepository.findAll();
    }

    public Professor findById(UUID id) {
        return professorRepository.findById(id).orElse(null);
    }

    public Professor update(UUID id, Professor obj) {
        Professor entity = professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
        updateProfessor(entity, obj);
        return professorRepository.save(entity);
    }

    public void deleteById(UUID id){
        if(!professorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado");
        }
        professorRepository.deleteById(id);
    }

    public void updateProfessor(Professor professor, Professor obj) {
        professor.setNome(obj.getNome());
    }
}
