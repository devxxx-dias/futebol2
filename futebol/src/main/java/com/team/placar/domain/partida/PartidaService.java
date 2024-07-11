package com.team.placar.domain.partida;

import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import com.team.placar.infra.securtiy.validacoes.partidas.ValidadorPartida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new ValidacaoException("Clube Mandante não encontrado pelo nome ou não está ativo"));
        var clubeVisitante = repository.findByNome(dados.nomeClubeVisitante())
                .orElseThrow(() -> new ValidacaoException("Clube Visitante não encontrado pelo nome ou não está ativo"));
        var estadio = repository.findEstadioByNome(dados.nomeEstadio())
                .orElseThrow(() -> new ValidacaoException("Estádio não encontrado pelo nome fornecido"));

        if (!clubeMandante.getLocalSede().equals(estadio.getCidade())) {
            throw new ValidacaoException("Verifique se o estádio é do clube da casa (mandante)");
        }
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
        var partida = repository.findById(id).orElseThrow(() -> new ValidacaoException("Partida não encontrada pelo ID"));
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
            var clube = repository.findByNome(clubeNome)
                    .orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo nome fornecido"));
            var partida = encontrarClubeId(clube.getId(), paginacao);
            return partida.map(DadosDetalhadamentoPartida::new);
        }

        if (estadioNome != null && !estadioNome.isEmpty()) {
            var estadio = repository.findEstadioByNome(estadioNome);
            var partida = encontrarEstadioId(estadio.get().getId(), paginacao);
            return partida.map(DadosDetalhadamentoPartida::new);
        }

        return page;
    }
}
