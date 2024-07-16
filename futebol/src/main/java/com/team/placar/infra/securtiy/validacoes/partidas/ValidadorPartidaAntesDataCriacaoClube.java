package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.PartidaRepository;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

@Component
public class ValidadorPartidaAntesDataCriacaoClube implements ValidadorPartida {
    @Autowired
    private PartidaRepository repository;

    @Override
    public void validar(DadosCadastroPartida dados) {
        var dataPartida = dados.dataHora().toLocalDate();

        var clubeMandante = repository.findByNome(dados.nomeClubeMandante())
                .orElseThrow(() -> new ValidacaoException("Clube Mandante não encontrado pelo nome fornecido" + dados.nomeClubeMandante()));
        var dataAnteriorMandante = repository
                .existsPartidasByClubeIdAndDataBefore(clubeMandante.getId(), dataPartida);

        var clubeVisitante = repository.findByNome(dados.nomeClubeVisitante())
                .orElseThrow(() -> new ValidacaoException("Clube Visitante não encontrado pelo nome fornecido" + dados.nomeClubeVisitante()));
        var dataAnteriorVisitante = repository
                .existsPartidasByClubeIdAndDataBefore(clubeVisitante.getId(), dataPartida);

        if (dataAnteriorMandante) {
            throw new ConflitException("Não é possível cadastrar uma partida antes da data de criacao do clube "
                    + dados.nomeClubeMandante());
        }

        if (dataAnteriorVisitante) {
            throw new ConflitException("Não é possível cadastrar uma partida antes da data de criacao do clube "
                    + dados.nomeClubeVisitante());
        }
    }

}
