package com.team.placar.domain.clube;

public record DadosRestrospctoClubeAdversarioDto(

        String nome,
        String adversario,
        Integer totalVitorias,
        Integer totalDerrotas,
        Integer totalEmpates,
        Integer totalGolsFeito,
        Integer totalGolsSofridos

) {

}
