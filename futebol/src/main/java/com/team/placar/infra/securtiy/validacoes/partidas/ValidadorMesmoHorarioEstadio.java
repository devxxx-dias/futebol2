package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.estadio.Estadio;
import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.PartidaRepository;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class ValidadorMesmoHorarioEstadio implements ValidadorPartida {

    @Autowired
    private PartidaRepository partidaRepository;

    @Override
    public void validar(DadosCadastroPartida dados) {
        LocalDateTime dataHora = dados.dataHora();
        LocalDateTime dataHoraInicio = dataHora.minusHours(12);
        LocalDateTime dataHoraFim = dataHora.plusHours(12);

        Estadio estadio = partidaRepository.findEstadioByNome(dados.nomeEstadio())
                .orElseThrow(() -> new ValidacaoException("Estádio não encontrado."));

        if (partidaRepository.existsPartidaByEstadio_IdAndDataHoraBetween(estadio.getId(), dataHoraInicio, dataHoraFim)) {
            throw new ConflitException("Já existe uma partida registrada nesse dia no estádio selecionado.");
        }
    }
}
