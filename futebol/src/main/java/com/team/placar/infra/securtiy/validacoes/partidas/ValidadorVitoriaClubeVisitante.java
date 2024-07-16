package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.Resultado;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorVitoriaClubeVisitante implements ValidadorPartida {
    @Override
    public void validar(DadosCadastroPartida dados) {
        if (dados.qtdeGolsClubeVisitante() > dados.qtdeGolsClubeMandante() && dados.resultadoClubeVisitante() != Resultado.VITORIA) {
            throw new ValidacaoException("O clube visitante venceu, logo seu resultado deve ser VITORIA e do mandante DERROTA");
        }
    }
}
