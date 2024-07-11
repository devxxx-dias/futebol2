package com.team.placar.domain.clube;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DadosClubeDetalhadamento(
        Long id,
        String nome,
        String siglaEstado,
        String localSede,
        LocalDate dataCriacao,
        String status
) {
    public DadosClubeDetalhadamento(Clube clube) {
        this(
                clube.getId(),
                clube.getNome(),
                clube.getSiglaEstado(),
                clube.getLocalSede(),
                clube.getDataCriacao(),
                (clube.getStatus() == true ? "Ativo" : "Inativo")
        );

    }
}
