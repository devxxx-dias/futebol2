package com.team.placar.domain.clube;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record DadosClubeCadastro(
        @NotBlank(message = "O nome do clube precisa ser inserido")
        String nome,
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "SiglaEstado deve ter exatamente 2 caracteres e apenas letras")
        String siglaEstado,
        @NotBlank
        String localSede,
        @PastOrPresent
        LocalDate dataCriacao,
        @NotNull(message = "Você deve apenas inserir os valores true ou false")
        Boolean status
) {

}
