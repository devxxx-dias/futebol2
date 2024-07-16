package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorClubeRepetido implements ValidadorPartida {
    @Override
    public void validar(DadosCadastroPartida dados) {
        if (dados.nomeClubeMandante() == dados.nomeClubeVisitante()) {
            throw new ValidacaoException("Você não pode cadastrar  um único clube para partida");
        }
    }
}
