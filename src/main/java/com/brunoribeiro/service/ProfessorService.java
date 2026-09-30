package com.brunoribeiro.service;

import com.brunoribeiro.entities.Professor;
import com.brunoribeiro.repositories.ProfessorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public List<Professor> findAll() {
        return professorRepository.findAll();
    }

    public Professor findById(UUID id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado, ID: " + id));
    }

    public Professor update(UUID id, Professor obj) {
        Professor entity = professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
        updateProfessor(entity, obj);
        return professorRepository.save(entity);
    }

    public void deleteById(UUID id) {
        professorRepository.delete(findById(id));
    }

    public void updateProfessor(Professor professor, Professor obj) {
        professor.setNome(obj.getNome());
        professor.setEmail(obj.getEmail());
        professor.setSenhaHash(obj.getSenhaHash());
        professor.setIntervaloMinimo(obj.getIntervaloMinimo());
    }

    public Professor create(Professor professor) {
        return professorRepository.save(professor);
    }
}
