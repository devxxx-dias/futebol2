package com.team.placar.domain.partida;

import java.time.LocalDateTime;

public record DadosDetalhadamentoPartida(
        Long id,
        String nomeClubeMandante,
        String nomeClubeVisitante,
        String nomeEstadio,
        Integer qtdeGolsClubeMandante,
        Integer qtdeGolsClubeVisitante,
        Resultado resultadoClubeMandante,
        Resultado resultadoClubeVisitante,
        LocalDateTime dataHora

) {

    public DadosDetalhadamentoPartida(Partida partida) {
        this(partida.getId(),
                partida.getClubeMandante().getNome(),
                partida.getClubeVisitante().getNome(),
                partida.getEstadio().getNome(),
                partida.getQtdeGolsClubeMandante(),
                partida.getQtdeGolsClubeVisitante(),
                partida.getResultadoClubeMandante(),
                partida.getResultadoClubeVisitante(),
                partida.getDataHora());
    }
}
