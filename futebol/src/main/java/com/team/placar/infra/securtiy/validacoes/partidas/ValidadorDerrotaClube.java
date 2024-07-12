package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.Resultado;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDerrotaClube  implements ValidadorPartida{
    @Override
    public void validar(DadosCadastroPartida dados) {
        if (dados.qtdeGolsClubeMandante() < dados.qtdeGolsClubeVisitante() && dados.resultadoClubeMandante() != Resultado.DERROTA) {
            throw new ValidacaoException("O clube mandante perdeu, logo seu resultado deve ser DERROTA e do visitante VITORIA");
        }

        if (dados.qtdeGolsClubeMandante() > dados.qtdeGolsClubeVisitante() && dados.resultadoClubeVisitante() != Resultado.DERROTA) {
            throw new ValidacaoException("O clube visitante perdeu, logo seu resultado deve ser DERROTA e do mandante VITORIA");
        }
    }
}
