package com.team.placar.domain.partida;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

public record DadosCadastroPartida(
        @NotBlank(message = "O Nome do clube Mandante deve ser infomado")
        String nomeClubeMandante,
        @NotBlank(message = "O Nome do clube Visitante deve ser infomado")
        String nomeClubeVisitante,
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
        @NotNull(message = "Infome a data e a hora da partida, formato YYYY-MM-DDTHH:MM")
        @PastOrPresent(message = "Não é possível salvar uma partida em uma data futura")
        LocalDateTime dataHora
) {
}
