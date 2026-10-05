package com.brunoribeiro.dto.response;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.entities.enums.DiaSemana;
import com.brunoribeiro.entities.enums.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record AlunoResponseDTO(
        UUID id,
        UUID professorId,
        UUID responsavelId,
        String nome,
        String email,
        String telefone,
        String endereco,
        FormaPagamento formaPagamento,
        BigDecimal valorMensal,
        DiaSemana diaSemanaPadrao,
        LocalTime horarioPadrao,
        Integer duracaoPadraoMin,
        BigDecimal valorPadrao
) {
    public static AlunoResponseDTO fromEntity(Aluno aluno) {
        if (aluno == null) {
            return null;
        }

        return new AlunoResponseDTO(
                aluno.getId(),
                aluno.getProfessorId(),
                aluno.getResponsavelId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getTelefone(),
                aluno.getEndereco(),
                aluno.getFormaPagamento(),
                aluno.getValorMensal(),
                aluno.getDiaSemanaPadrao(),
                aluno.getHorarioPadrao(),
                aluno.getDuracaoPadraoMin(),
                aluno.getValorPadrao()
        );
    }
}
