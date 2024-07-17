package com.team.placar.infra.securtiy.validacoes.estadios;

import com.team.placar.domain.estadio.DadosCadastroEstadio;

public interface ValidadorEstadio {
    public void validar(DadosCadastroEstadio dados);
}
