package com.team.placar.domain.partida;

import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import com.team.placar.infra.securtiy.validacoes.partidas.ValidadorPartida;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PartidaService {
    @Autowired
    private PartidaRepository repository;

    @Autowired
    private List<ValidadorPartida> validadores;

    public Partida salvar(DadosCadastroPartida dados) {
        validadores.forEach(d -> d.validar(dados));

        var clubeMandante = repository.findByNome(dados.nomeClubeMandante())
                .orElseThrow(() -> new EntityNotFoundException("Clube Mandante não encontrado pelo nome ou não está ativo"));
        var clubeVisitante = repository.findByNome(dados.nomeClubeVisitante())
                .orElseThrow(() -> new EntityNotFoundException("Clube Visitante não encontrado pelo nome ou não está ativo"));
        var estadio = repository.findEstadioByNome(dados.nomeEstadio())
                .orElseThrow(() -> new EntityNotFoundException("Estádio não encontrado pelo nome fornecido"));

        var partida = new Partida(
                clubeMandante,
                clubeVisitante,
                estadio,
                dados.qtdeGolsClubeMandante(),
                dados.qtdeGolsClubeVisitante(),
                dados.resultadoClubeMandante(),
                dados.resultadoClubeVisitante(),
                dados.dataHora());
        repository.save(partida);
        return partida;
    }

    public Partida validarId(Long id) {
        var partida = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Partida não encontrada pelo ID"));
        return partida;
    }

    public Partida atualizarPartidaPeloId(DadosCadastroPartida dados, Long id) {
        var partida = validarId(id);
        validadores.forEach(d -> d.validar(dados));
        partida.atualizarInformacoes(id, dados);
        return partida;
    }

    public Page<Partida> encontrarClubeId(Long clubeId, Pageable paginacao) {
        return repository.findPartidaByClubeId(clubeId, paginacao);
    }

    public Page<Partida> encontrarEstadioId(Long estadioId, Pageable paginacao) {
        return repository.findPartidaByEstadioId(estadioId, paginacao);
    }


    public Page filtrarParams(String clubeNome, String estadioNome, Pageable paginacao) {
        var page = repository.findAll(paginacao).map(DadosDetalhadamentoPartida::new);


        if (clubeNome != null && !clubeNome.isEmpty()) {
            var clube = repository.findClube(clubeNome);
            if( clube != null){
                var partida = encontrarClubeId(clube.getId(), paginacao);
                return partida.map(DadosDetalhadamentoPartida::new);
            }
            else {
                return new PageImpl<>(Collections.emptyList(), paginacao, 0);
            }
        }

        if (estadioNome != null && !estadioNome.isEmpty()) {
            var estadio = repository.findEstadioByNome(estadioNome);
            var partida = encontrarEstadioId(estadio.get().getId(), paginacao);
            return partida.map(DadosDetalhadamentoPartida::new);
        }


        return page;
    }
}
