package com.team.placar.domain.estadio;

import jakarta.validation.constraints.NotBlank;

public record DadosDetalhadamentoEstadio(
        String nome,
        String cidade,
        String siglaEstado
) {
    public DadosDetalhadamentoEstadio(Estadio estadio) {
        this(estadio.getNome(), estadio.getCidade(), estadio.getSiglaEstado());
    }
}
