package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.Resultado;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorEmpateNaPartida implements ValidadorPartida {
    @Override
    public void validar(DadosCadastroPartida dados) {
        if (dados.qtdeGolsClubeMandante() == dados.qtdeGolsClubeVisitante() && dados.resultadoClubeVisitante() != Resultado.EMPATE && dados.resultadoClubeMandante() != Resultado.EMPATE) {
            throw new ValidacaoException("Houve um empate, logo ambos os resultados devem ser EMPATE");
    }
    }
}
