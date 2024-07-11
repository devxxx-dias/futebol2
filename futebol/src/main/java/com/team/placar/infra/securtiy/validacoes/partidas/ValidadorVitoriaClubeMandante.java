package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.Resultado;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorVitoriaClubeMandante implements ValidadorPartida {
    @Override
    public void validar(DadosCadastroPartida dados) {
        if (dados.qtdeGolsClubeMandante() > dados.qtdeGolsClubeVisitante() && dados.resultadoClubeMandante() != Resultado.VITORIA) {
            throw new ValidacaoException("O clube mandante venceu, logo seu resultado deve ser VITORIA e do visitante DERROTA");
        }
    }
}
