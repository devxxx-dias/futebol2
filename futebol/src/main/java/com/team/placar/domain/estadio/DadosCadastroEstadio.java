package com.team.placar.domain.estadio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DadosCadastroEstadio(
        @NotBlank(message = "O nome do estadio precisa ser infomado")
        String nome,
        String cidade,
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "SiglaEstado deve ter exatamente 2 caracteres e apenas letras")
        String siglaEstado
) {
}
