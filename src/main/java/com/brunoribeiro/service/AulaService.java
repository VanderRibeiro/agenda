package com.brunoribeiro.service;

import com.brunoribeiro.entities.Aula;
import com.brunoribeiro.entities.enums.StatusAula;
import com.brunoribeiro.exception.BusinessException;
import com.brunoribeiro.exception.ResourceNotFoundException;
import com.brunoribeiro.repositories.AulaRepository;
import com.brunoribeiro.repositories.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AulaService extends BaseService {

    private static final Set<StatusAula> STATUS_IGNORADOS = Set.of(StatusAula.CANCELADA, StatusAula.ADIADA);

    private final AulaRepository aulaRepository;
    private final ProfessorRepository professorRepository;

    public AulaService(AulaRepository aulaRepository, ProfessorRepository professorRepository) {
        this.aulaRepository = aulaRepository;
        this.professorRepository = professorRepository;
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
        validarConflitoDeHorario(entity);
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
        aula.setDuracaoMinutos(obj.getDuracaoMinutos());
        aula.setValor(obj.getValor());
        aula.setStatus(obj.getStatus());
        aula.setPago(obj.getPago());
        aula.setDatePagamento(obj.getDatePagamento());

        if (aula.getHorarioInicio() != null && aula.getDuracaoMinutos() != null) {
            aula.setHorarioFim(aula.getHorarioInicio().plusMinutes(aula.getDuracaoMinutos()));
        }
    }

    public Aula create(Aula aula) {
        if (aula == null) {
            throw new BusinessException("Dados da aula não informados.");
        }

        aula.setProfessorId(requireCurrentProfessorId());
        aula.setStatus(StatusAula.AGENDADA);

        if (aula.getHorarioInicio() != null && aula.getDuracaoMinutos() != null) {
            aula.setHorarioFim(aula.getHorarioInicio().plusMinutes(aula.getDuracaoMinutos()));
        }

        validarConflitoDeHorario(aula);
        return aulaRepository.save(aula);
    }

    public Aula remarcar(UUID id, Aula novaAula) {
        if (novaAula == null) {
            throw new BusinessException("Dados da aula não informados.");
        }

        Aula aulaOriginal = findById(id);
        ensureProfessorAccess(aulaOriginal.getProfessorId(), "aula");

        novaAula.setProfessorId(aulaOriginal.getProfessorId());
        novaAula.setAlunoId(novaAula.getAlunoId() != null ? novaAula.getAlunoId() : aulaOriginal.getAlunoId());
        novaAula.setAulaOriginalId(id);
        novaAula.setStatus(StatusAula.AGENDADA);

        if (novaAula.getHorarioInicio() != null && novaAula.getDuracaoMinutos() != null) {
            novaAula.setHorarioFim(novaAula.getHorarioInicio().plusMinutes(novaAula.getDuracaoMinutos()));
        }

        validarConflitoDeHorario(novaAula);

        aulaOriginal.setStatus(StatusAula.ADIADA);
        aulaRepository.save(aulaOriginal);
        return aulaRepository.save(novaAula);
    }

    private void validarConflitoDeHorario(Aula aula) {
        if (aula.getData() == null || aula.getHorarioInicio() == null || aula.getDuracaoMinutos() == null) {
            return;
        }

        UUID professorId = aula.getProfessorId();
        int intervalo = professorRepository.findById(professorId)
                .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado, ID: " + professorId))
                .getIntervaloMinimo();

        LocalTime inicio = aula.getHorarioInicio();
        LocalTime fim = inicio.plusMinutes(aula.getDuracaoMinutos());
        LocalTime inicioComIntervalo = inicio.minusMinutes(intervalo);
        LocalTime fimComIntervalo = fim.plusMinutes(intervalo);

        boolean conflito = aulaRepository
                .findOverlappingAulas(professorId, aula.getData(), inicioComIntervalo, fimComIntervalo, STATUS_IGNORADOS)
                .stream()
                .anyMatch(existing -> existing.getId() != null && !existing.getId().equals(aula.getId()));

        if (conflito) {
            throw new BusinessException("O horário solicitado está próximo demais de outro horário já existente. Escolha outro horário ou acesse Configurações > Horários > Tempo de intervalo.");
        }
    }
}
