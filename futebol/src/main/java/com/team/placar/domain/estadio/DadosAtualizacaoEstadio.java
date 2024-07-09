package com.team.placar.domain.estadio;

import jakarta.validation.constraints.NotBlank;

public record DadosAtualizacaoEstadio (
        @NotBlank(message = "O nome do estadio precisa ser infomado")
        String nome,
        String cidade,
        @NotBlank(message = "A sigla do estado da localização do estádio precisa ser informada")
        String siglaEstado
) {
}
