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
import java.util.Objects;
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

        boolean conflito = aulaRepository
                .findAllByProfessorIdAndDataAndStatusNotIn(professorId, aula.getData(), STATUS_IGNORADOS).stream()
                .filter(existing -> !Objects.equals(existing.getId(), aula.getId()))
                .anyMatch(existing -> existeConflitoCom(existing, inicio, fim, intervalo));

        if (conflito) {
            throw new BusinessException("O horário solicitado está próximo demais de outro horário já existente. Escolha outro horário ou acesse Configurações > Horários > Tempo de intervalo.");
        }
    }

    private boolean existeConflitoCom(Aula existente, LocalTime inicio, LocalTime fim, int intervalo) {
        if (existente.getHorarioInicio() == null || existente.getDuracaoMinutos() == null) {
            return false;
        }
        LocalTime existenteInicio = existente.getHorarioInicio();
        LocalTime existenteFim = existenteInicio.plusMinutes(existente.getDuracaoMinutos());

        return inicio.isBefore(existenteFim.plusMinutes(intervalo))
                && fim.isAfter(existenteInicio.minusMinutes(intervalo));
    }
}
