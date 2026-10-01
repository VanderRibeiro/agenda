package com.brunoribeiro.service;

import com.brunoribeiro.entities.Aula;
import com.brunoribeiro.exception.BusinessException;
import com.brunoribeiro.exception.ResourceNotFoundException;
import com.brunoribeiro.repositories.AulaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AulaService {

    private final AulaRepository aulaRepository;

    public AulaService(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }

    public List<Aula> findAll() {
        return aulaRepository.findAll();
    }

    public Aula findById(UUID id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada, ID: " + id));
    }

    public Aula update(UUID id, Aula obj) {
        if (obj == null) {
            throw new BusinessException("Dados da aula não informados.");
        }

        Aula entity = aulaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada, ID: " + id));
        updateAula(entity, obj);
        return aulaRepository.save(entity);
    }

    public void deleteById(UUID id) {
        aulaRepository.delete(findById(id));
    }

    public void updateAula(Aula aula, Aula obj) {
        aula.setProfessorId(obj.getProfessorId());
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
        return aulaRepository.save(aula);
    }
}
