package com.team.placar.domain.clube;

import com.team.placar.infra.securtiy.validarSigla.ValidBrazilState;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DadosClubeCadastro(
        @NotBlank(message = "O nome do clube precisa ser inserido")
        @Size(min = 2, message = "O nome clube dever possuir no mínimo 2 letras")
        String nome,
        @ValidBrazilState
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "SiglaEstado deve ter exatamente 2 caracteres e apenas letras")
        String siglaEstado,
        @NotBlank(message = "A localidade da sede deve ser inserida")
        String localSede,
        @NotNull(message = "O campo dataCriacao deve ser preenchido")
        @PastOrPresent(message = "Só é permitido inserir uma data no passado")
        LocalDate dataCriacao,
        @NotNull(message = "Você deve apenas inserir os valores true ou false")
        Boolean status
) {

}


