package com.team.placar.domain.estadio;

import com.team.placar.infra.securtiy.anotacoes.semNumeros.NoNumbers;
import com.team.placar.infra.securtiy.anotacoes.validarSigla.ValidBrazilState;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroEstadio(
        @NotBlank(message = "O nome do estadio precisa ser infomado")
        @Size(min = 3, message = "O Nome do clube não pode ser menor que 3 letras")
        String nome,
        @Nullable
        @NoNumbers
        String cidade,
        @Nullable
        @ValidBrazilState
        String siglaEstado
) {
}
