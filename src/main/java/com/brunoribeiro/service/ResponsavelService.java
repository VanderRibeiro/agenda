package com.brunoribeiro.service;

import com.brunoribeiro.entities.Responsavel;
import com.brunoribeiro.repositories.ResponsavelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ResponsavelService {

    @Autowired
    public ResponsavelRepository  responsavelRepository;

    public List<Responsavel> findAll() {
        return responsavelRepository.findAll();
    }

    public Responsavel findById(UUID id) {
        return responsavelRepository.findById(id).orElse(null);
    }

    public void deleteById(UUID id) {
        if(!responsavelRepository.existsById(id)) {
            throw new RuntimeException("Responsável não encontrado");
        }
        responsavelRepository.deleteById(id);
    }

    public Responsavel update(UUID id, Responsavel obj) {
        Responsavel entity = responsavelRepository.findById(id).
            orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Responsável não encontrado"));
        updateResponsavel(entity, obj);
        return responsavelRepository.save(entity);
    }

    public void updateResponsavel(Responsavel responsavel, Responsavel obj) {
        responsavel.setNome(obj.getNome());
    }
}
