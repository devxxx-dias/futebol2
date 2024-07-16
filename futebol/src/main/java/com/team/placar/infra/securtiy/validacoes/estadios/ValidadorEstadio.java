package com.team.placar.infra.securtiy.validacoes.estadios;

import com.team.placar.domain.estadio.DadosCadastroEstadio;
import com.team.placar.domain.estadio.Estadio;

public interface ValidadorEstadio {
    public void validar(DadosCadastroEstadio dados);
}
