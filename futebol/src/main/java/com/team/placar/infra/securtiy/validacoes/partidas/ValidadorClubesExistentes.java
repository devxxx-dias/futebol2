package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.PartidaRepository;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorClubesExistentes implements ValidadorPartida {
    @Autowired
    private PartidaRepository partidaRepository;


    @Override
    public void validar(DadosCadastroPartida dados) {
        var clubeMandante = partidaRepository.findByNome(dados.nomeClubeMandante())
                .orElseThrow(() -> new ConflitException("Clube mandante não localizado pelo nome: " + dados.nomeClubeMandante()));

        var clubeVisitante = partidaRepository.findByNome(dados.nomeClubeVisitante())
                .orElseThrow(() -> new ConflitException("Clube visitante não localizado pelo nome: " + dados.nomeClubeVisitante()));
    }
}
