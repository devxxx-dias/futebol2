package com.team.placar.domain.partida;

import com.team.placar.domain.clube.ClubeRepository;
import com.team.placar.domain.clube.ClubeService;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import com.team.placar.infra.securtiy.validacoes.partidas.ValidadorPartida;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PartidaService {
    @Autowired
    private PartidaRepository repository;

    @Autowired
    private ClubeService clubeService;

    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    private List<ValidadorPartida> validadores;

    public Partida salvar(DadosCadastroPartida dados) {
        validadores.forEach(d -> d.validar(dados));

        var clubeMandante = repository.findByNome(dados.nomeClubeMandante())
                .orElseThrow(() -> new ConflitException("Clube mandante não localizado pelo nome: " + dados.nomeClubeMandante()));
        var clubeVisitante = repository.findByNome(dados.nomeClubeVisitante())
                .orElseThrow(() -> new ConflitException("Clube visitante não localizado pelo nome: " + dados.nomeClubeVisitante()));
        var estadio = repository.findEstadioByNome(dados.nomeEstadio())
                .orElseThrow(() -> new ConflitException("Estadio não localizado pelo nome: " + dados.nomeEstadio()));

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
        partida.atualizarInformacoes(dados);
        return partida;
    }

    public Page<Partida> encontrarClubeId(Long clubeId, Pageable paginacao) {
        return repository.findPartidaByClubeId(clubeId, paginacao);
    }

    public Page<Partida> encontrarEstadioId(Long estadioId, Pageable paginacao) {
        return repository.findPartidaByEstadioId(estadioId, paginacao);
    }


    public Page filtrarParams(String clubeNome,
                              String estadioNome,
                              String ranking,
                              Pageable paginacao) {

        if (clubeNome != null && !clubeNome.isEmpty()) {
            var clube = repository.findClube(clubeNome);
            if (clube != null) {
                var partida = encontrarClubeId(clube.getId(), paginacao);
                return partida.map(DadosDetalhadamentoPartida::new);
            } else {
                return new PageImpl<>(Collections.emptyList(), paginacao, 0);
            }
        }

        if (estadioNome != null && !estadioNome.isEmpty()) {
            var estadio = repository.findEstadioByNome(estadioNome);
            var partida = encontrarEstadioId(estadio.get().getId(), paginacao);
            return partida.map(DadosDetalhadamentoPartida::new);
        }

        if (ranking.equals("goleadas")) {
            var goleadas = repository.findPartidasByGoleadas(paginacao);
            return goleadas.map(DadosDetalhadamentoPartida::new);
        }

        if (ranking != null && !ranking.isEmpty()) {
            var obterRanking = getRanking(ranking, paginacao);
            return obterRanking;
        }

        var page = repository.findAll(paginacao).map(DadosDetalhadamentoPartida::new);
        return page;
    }

    public Page<ClubeRankingDTO> getRanking(String criteria, Pageable paginacao) {
        switch (criteria.toLowerCase()) {
            case "total_jogos":
                return repository.findRankingByTotalJogos(paginacao);
            case "total_vitorias":
                return repository.findRankingByTotalVitorias(paginacao);
            case "total_gols":
                return repository.findRankingByTotalGols(paginacao);
            case "total_pontos":
                return repository.findRankingByTotalPontos(paginacao);
            default:
                return new PageImpl<>(Collections.emptyList(), paginacao, 0);
        }
    }


    public Map listarPartidasRetro(Long idClube, Long idClubeAdversario) {
        clubeService.buscar(idClube);
        clubeService.buscar(idClubeAdversario);

        List<Partida> retrospectiva = repository.findRestrospecto(idClube, idClubeAdversario);
        var restrospectoClube = clubeService.efeituarRestrospectivaAdversario(idClube, idClubeAdversario);
        var restrospectoClubeAdversario = clubeService.efeituarRestrospectivaAdversario(idClubeAdversario, idClube);


        Map<String, Object> response = new HashMap<>();
        response.put("ListaPartida", retrospectiva.stream().map(DadosDetalhadamentoPartida::new));
        response.put("RetrospectoClube", restrospectoClube);
        response.put("RetrospectoAdversario", restrospectoClubeAdversario);

        return response;
    }

}
