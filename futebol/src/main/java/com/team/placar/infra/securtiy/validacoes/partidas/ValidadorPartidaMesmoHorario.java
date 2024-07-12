package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.PartidaRepository;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorPartidaMesmoHorario implements ValidadorPartida{
   @Autowired
   private PartidaRepository partidaRepository;

    @Override
    public void validar(DadosCadastroPartida dados) {
        var dataMaxIntervalo = dados.dataHora().plusMinutes(175);
        var dataMinIntervalo = dados.dataHora().minusMinutes(175);

        if(partidaRepository.existsByDataHoraBetween(dataMinIntervalo, dataMaxIntervalo)){
            throw new ValidacaoException("Já Existe uma partida registrada nesse horário no estádio selecionado");
        }
    }
}
