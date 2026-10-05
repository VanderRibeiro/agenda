package com.brunoribeiro.dto.request;

import com.brunoribeiro.entities.Aluno;
import com.brunoribeiro.entities.enums.DiaSemana;
import com.brunoribeiro.entities.enums.FormaPagamento;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record AlunoRequestDTO(
        @NotNull(message = "O professor é obrigatório")
        UUID professorId,

        UUID responsavelId,

        @NotBlank(message = "O nome do aluno é obrigatório")
        @Size(max = 100, message = "O nome do aluno deve ter no máximo 100 caracteres")
        String nome,

        @Email(message = "Informe um email válido")
        @Size(max = 150, message = "O email do aluno deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "O telefone do aluno é obrigatório")
        @Size(max = 30, message = "O telefone do aluno deve ter no máximo 30 caracteres")
        String telefone,

        @Size(max = 255, message = "O endereço do aluno deve ter no máximo 255 caracteres")
        String endereco,

        @NotNull(message = "A forma de pagamento do aluno é obrigatória")
        String formaPagamento,

        @NotNull(message = "O valor mensal do aluno é obrigatório")
        BigDecimal valorMensal,

        String diaSemanaPadrao,

        LocalTime horarioPadrao,

        Integer duracaoPadraoMin,

        BigDecimal valorPadrao
) {
    @AssertTrue(message = "O email do aluno é obrigatório quando não há responsável")
    public boolean isEmailObrigatorioQuandoSemResponsavel() {
        return responsavelId != null || (email != null && !email.isBlank());
    }

    public Aluno toEntity() {
        Aluno aluno = new Aluno();
        aluno.setProfessorId(this.professorId);
        aluno.setResponsavelId(this.responsavelId);
        aluno.setNome(this.nome);
        aluno.setEmail(this.email);
        aluno.setTelefone(this.telefone);
        aluno.setEndereco(this.endereco);
        aluno.setFormaPagamento(this.formaPagamento != null ? FormaPagamento.valueOf(this.formaPagamento.trim().toUpperCase()) : null);
        aluno.setValorMensal(this.valorMensal);
        aluno.setDiaSemanaPadrao(this.diaSemanaPadrao != null && !this.diaSemanaPadrao.isBlank()
                ? DiaSemana.valueOf(this.diaSemanaPadrao.trim().toUpperCase())
                : null);
        aluno.setHorarioPadrao(this.horarioPadrao);
        aluno.setDuracaoPadraoMin(this.duracaoPadraoMin);
        aluno.setValorPadrao(this.valorPadrao);
        return aluno;
    }
}
