package com.brunoribeiro.service;

import com.brunoribeiro.entities.Responsavel;
import com.brunoribeiro.exception.BusinessException;
import com.brunoribeiro.exception.ResourceNotFoundException;
import com.brunoribeiro.repositories.ResponsavelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ResponsavelService extends BaseService {

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

    public Responsavel findById(UUID id) {
        Responsavel responsavel = responsavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado, ID: " + id));
        ensureProfessorAccess(responsavel.getProfessorId(), "responsável");
        return responsavel;
    }

    public void deleteById(UUID id) {
        Responsavel responsavel = responsavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado, ID: " + id));
        ensureProfessorAccess(responsavel.getProfessorId(), "responsável");
        responsavelRepository.delete(responsavel);
    }

    public Responsavel update(UUID id, Responsavel obj) {
        if (obj == null) {
            throw new BusinessException("Dados do responsável não informados.");
        }

        Responsavel entity = responsavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado, ID: " + id));
        ensureProfessorAccess(entity.getProfessorId(), "responsável");
        updateResponsavel(entity, obj);
        entity.setProfessorId(requireCurrentProfessorId());
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
        responsavel.setProfessorId(requireCurrentProfessorId());
        return responsavelRepository.save(responsavel);
    }
}
