package com.team.placar.domain.clube;

import java.time.LocalDate;

public record DadosClubeDetalhadamento (
        Long id,
        String nome,
        String siglaEstado,
        String localSede,
        LocalDate dataCriacao,
        Boolean status
){
    public DadosClubeDetalhadamento(Clube clube) {
        this(clube.getId(), clube.getNome(), clube.getSiglaEstado(), clube.getLocalSede(), clube.getDataCriacao(), clube.getStatus());
    }
}
