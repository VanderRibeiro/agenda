package com.brunoribeiro.service;

import com.brunoribeiro.entities.Aula;
import com.brunoribeiro.exception.BusinessException;
import com.brunoribeiro.exception.ResourceNotFoundException;
import com.brunoribeiro.repositories.AulaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AulaService extends BaseService {

    private final AulaRepository aulaRepository;

    public AulaService(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }

    public List<Aula> findAll() {
        return aulaRepository.findAllByProfessorId(requireCurrentProfessorId());
    }

    public Aula findById(UUID id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada, ID: " + id));
        ensureProfessorAccess(aula.getProfessorId(), "aula");
        return aula;
    }

    public Aula update(UUID id, Aula obj) {
        if (obj == null) {
            throw new BusinessException("Dados da aula não informados.");
        }

        Aula entity = aulaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada, ID: " + id));
        ensureProfessorAccess(entity.getProfessorId(), "aula");
        updateAula(entity, obj);
        return aulaRepository.save(entity);
    }

    public void deleteById(UUID id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada, ID: " + id));
        ensureProfessorAccess(aula.getProfessorId(), "aula");
        aulaRepository.delete(aula);
    }

    public void updateAula(Aula aula, Aula obj) {
        aula.setAlunoId(obj.getAlunoId());
        aula.setAulaOriginalId(obj.getAulaOriginalId());
        aula.setData(obj.getData());
        aula.setHorarioInicio(obj.getHorarioInicio());
        aula.setHorarioFim(obj.getHorarioFim());
        aula.setDuracaoMinutos(obj.getDuracaoMinutos());
        aula.setValor(obj.getValor());
        aula.setStatus(obj.getStatus());
        aula.setPago(obj.getPago());
        aula.setDatePagamento(obj.getDatePagamento());
    }

    public Aula create(Aula aula) {
        if (aula == null) {
            throw new BusinessException("Dados da aula não informados.");
        }
        aula.setProfessorId(requireCurrentProfessorId());
        return aulaRepository.save(aula);
    }
}
