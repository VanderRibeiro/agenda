package com.brunoribeiro.service;

import com.brunoribeiro.entities.Responsavel;
import com.brunoribeiro.exception.BusinessException;
import com.brunoribeiro.exception.ResourceNotFoundException;
import com.brunoribeiro.repositories.ResponsavelRepository;
import com.brunoribeiro.security.ProfessorUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;

    public ResponsavelService(ResponsavelRepository responsavelRepository) {
        this.responsavelRepository = responsavelRepository;
    }

    public List<Responsavel> findAll() {
        UUID professorId = getCurrentProfessorId();
        if (professorId == null) {
            return List.of();
        }
        return responsavelRepository.findAllByProfessorId(professorId);
    }

    private UUID getCurrentProfessorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ProfessorUserDetails userDetails)) {
            return null;
        }
        return userDetails.getProfessor().getId();
    }

    public Responsavel findById(UUID id) {
        return responsavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado, ID: " + id));
    }

    public void deleteById(UUID id) {
        responsavelRepository.delete(findById(id));
    }

    public Responsavel update(UUID id, Responsavel obj) {
        if (obj == null) {
            throw new BusinessException("Dados do responsável não informados.");
        }

        Responsavel entity = responsavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado, ID: " + id));
        updateResponsavel(entity, obj);
        return responsavelRepository.save(entity);
    }

    public void updateResponsavel(Responsavel responsavel, Responsavel obj) {
        responsavel.setNome(obj.getNome());
        responsavel.setProfessorId(obj.getProfessorId());
        responsavel.setTelefone(obj.getTelefone());
    }

    public Responsavel create(Responsavel responsavel) {
        if (responsavel == null) {
            throw new BusinessException("Dados do responsável não informados.");
        }
        return responsavelRepository.save(responsavel);
    }
}
