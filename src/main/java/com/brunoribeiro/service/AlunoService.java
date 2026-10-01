package com.brunoribeiro.service;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.exception.BusinessException;
import com.brunoribeiro.exception.ResourceNotFoundException;
import com.brunoribeiro.repositories.AlunoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public List<Aluno> findAll() {
        return alunoRepository.findAll();
    }

    public Aluno findById(UUID id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado, ID: " + id));
    }

    public Aluno update(UUID id, Aluno obj) {
        if (obj == null) {
            throw new BusinessException("Dados do aluno não informados.");
        }

        Aluno entity = alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado, ID: " + id));
        updateAluno(entity, obj);
        return alunoRepository.save(entity);
    }

    public void deleteById(UUID id) {
        alunoRepository.delete(findById(id));
    }

    public void updateAluno(Aluno aluno, Aluno obj) {
        aluno.setProfessorId(obj.getProfessorId());
        aluno.setResponsavelId(obj.getResponsavelId());
        aluno.setNome(obj.getNome());
        aluno.setTelefone(obj.getTelefone());
        aluno.setEndereco(obj.getEndereco());
        aluno.setFormaPagamento(obj.getFormaPagamento());
        aluno.setValorMensal(obj.getValorMensal());
        aluno.setDiaSemanaPadrao(obj.getDiaSemanaPadrao());
        aluno.setHorarioPadrao(obj.getHorarioPadrao());
        aluno.setDuracaoPadraoMin(obj.getDuracaoPadraoMin());
        aluno.setValorPadrao(obj.getValorPadrao());
    }

    public Aluno create(Aluno aluno) {
        if (aluno == null) {
            throw new BusinessException("Dados do aluno não informados.");
        }
        return alunoRepository.save(aluno);
    }
}
