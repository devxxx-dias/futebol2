package com.team.placar.domain.partida;

public record ClubeRankingDTO(
        Long clubeId,
        String clubeNome,
        Long totalJogos,
        Long totalVitorias,
        Long totalGols,
        Long totalPontos
) {}

