package com.brunoribeiro.service;

import com.brunoribeiro.entities.Responsavel;
import com.brunoribeiro.repositories.ResponsavelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;

    public ResponsavelService(ResponsavelRepository responsavelRepository) {
        this.responsavelRepository = responsavelRepository;
    }

    public List<Responsavel> findAll() {
        return responsavelRepository.findAll();
    }

    public Responsavel findById(UUID id) {
        return responsavelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Responsável não encontrado, ID: " + id));
    }

    public void deleteById(UUID id) {
        responsavelRepository.delete(findById(id));
    }

    public Responsavel update(UUID id, Responsavel obj) {
        Responsavel entity = responsavelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Responsável não encontrado"));
        updateResponsavel(entity, obj);
        return responsavelRepository.save(entity);
    }

    public void updateResponsavel(Responsavel responsavel, Responsavel obj) {
        responsavel.setNome(obj.getNome());
        responsavel.setProfessorId(obj.getProfessorId());
        responsavel.setTelefone(obj.getTelefone());
    }

    public Responsavel create(Responsavel responsavel) {
        return responsavelRepository.save(responsavel);
    }
}
