package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;

public interface ValidadorPartida {
    public void validar(DadosCadastroPartida dados);

}
