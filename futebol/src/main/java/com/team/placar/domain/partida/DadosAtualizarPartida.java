package com.team.placar.domain.partida;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;


public record DadosAtualizarPartida(
        @NotBlank(message = "O Nome do clube Mandante deve ser infomado")
        String nomeClubeMandante,
        @NotBlank(message = "O Nome do clube Visitante deve ser infomado")
        String nomeClubeVisitante,
        @NotBlank(message = "O nome do estádio deve ser inserido")
        String nomeEstadio,
        @NotNull(message = "A quantidade de gols do clube mandante deve ser informado")
        @Min(value = 0, message = "A quantidade de gols do clube mandante não pode ser negativa")
        Integer qtdeGolsClubeMandante,
        @NotNull(message = "A quantidade de gols do clube visitante deve ser informado")
        @Min(value = 0, message = "A quantidade de gols do clube visitante não pode ser negativa")
        Integer qtdeGolsClubeVisitante,
        @NotNull(message = "Defina o resultado final do Clube Mandante - VITORIA, DERROTA OU EMPATE ")
        Resultado resultadoClubeMandante,
        @NotNull(message = "Defina o resultado final do Clube Visitante - VITORIA, DERROTA OU EMPATE ")
        Resultado resultadoClubeVisitante,
        @NotNull(message = "Infome a data e a hora da partida, formato dd-MM-YYYYTHH:MM")
        @PastOrPresent(message = "Não é possível salvar uma partida em uma data futura")
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHora
) {
}
