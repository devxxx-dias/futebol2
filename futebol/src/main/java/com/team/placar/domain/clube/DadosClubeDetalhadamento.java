package com.team.placar.domain.clube;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DadosClubeDetalhadamento(
        Long id,
        String nome,
        String siglaEstado,
        String localSede,
        LocalDate dataCriacao,
        Boolean status
) implements Detalhadamento{
    public DadosClubeDetalhadamento(Clube clube) {
        this(
                clube.getId(),
                clube.getNome(),
                clube.getSiglaEstado(),
                clube.getLocalSede(),
                clube.getDataCriacao(),
                clube.getStatus()
        );

    }
}
