package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.Partida;
import com.team.placar.domain.partida.PartidaRepository;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorPartidaHorariosProximos implements ValidadorPartida{
   @Autowired
   private PartidaRepository partidaRepository;

    @Override
    public void validar(DadosCadastroPartida dados) {
        var clubeMandante = partidaRepository.findByNome(dados.nomeClubeMandante()).orElseThrow();
        var clubeVisitante = partidaRepository.findByNome(dados.nomeClubeVisitante()).orElseThrow();

        if(partidaRepository.existsPartidasWithin48Hours(clubeMandante.getId(), dados.dataHora())){
            throw new ConflitException("Já Existe uma partida registrada nesse horário no estádio selecionado para o clube mandante.");
        }

        if(partidaRepository.existsPartidasWithin48Hours(clubeVisitante.getId(), dados.dataHora())){
            throw new ConflitException("Já Existe uma partida registrada nesse horário no estádio selecionado para o clube visitante.");
        }
    }
}
