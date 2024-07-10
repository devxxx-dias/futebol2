package com.team.placar.domain.partida;

import com.team.placar.domain.clube.Clube;
import com.team.placar.domain.clube.DadosClubeDetalhadamento;
import com.team.placar.infra.securtiy.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PartidaService {
    @Autowired
    private PartidaRepository repository;

    public Partida validarDados(DadosCadastroPartida dados) {
        if (dados.qtdeGolsClubeMandante() > dados.qtdeGolsClubeVisitante() && dados.resultadoClubeMandante() != Resultado.VITORIA) {
            throw new ValidacaoException("O clube mandante venceu, logo seu resultado deve ser VITORIA e do visitante DERROTA");
        } else if (dados.qtdeGolsClubeVisitante() > dados.qtdeGolsClubeMandante() && dados.resultadoClubeVisitante() != Resultado.VITORIA) {
            throw new ValidacaoException("O clube visitante venceu, logo seu resultado deve ser VITORIA e do mandante DERROTA");
        } else if (dados.qtdeGolsClubeMandante() == dados.qtdeGolsClubeVisitante() && dados.resultadoClubeVisitante() != Resultado.EMPATE && dados.resultadoClubeMandante() != Resultado.EMPATE) {
            throw new ValidacaoException("Houve um empate, logo ambos os resultados devem ser EMPATE");
        } else if (dados.nomeClubeMandante() == dados.nomeClubeVisitante()) {
            throw new ValidacaoException("Você não pode cadastrar  um único clube para partida");
        }

        var clubeMandante = repository.findByNome(dados.nomeClubeMandante())
                .orElseThrow(() -> new ValidacaoException("Clube Mandante não encontrado pelo nome ou não está ativo"));
        var clubeVisitante = repository.findByNome(dados.nomeClubeVisitante())
                .orElseThrow(() -> new ValidacaoException("Clube Visitante não encontrado pelo nome ou não está ativo"));
        var estadio = repository.findEstadioByNome(dados.nomeEstadio())
                .orElseThrow(() -> new ValidacaoException("Estádio não encontrado pelo nome fornecido"));

        if(!clubeMandante.getLocalSede().equals(estadio.getCidade())){
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

    public Partida validarDadosID(DadosAtualizarPartida dados, Long id) {
        if (dados.qtdeGolsClubeMandante() > dados.qtdeGolsClubeVisitante() && dados.resultadoClubeMandante() != Resultado.VITORIA) {
            throw new ValidacaoException("O clube mandante venceu, logo seu resultado deve ser VITORIA e do visitante DERROTA");
        } else if (dados.qtdeGolsClubeVisitante() > dados.qtdeGolsClubeMandante() && dados.resultadoClubeVisitante() != Resultado.VITORIA) {
            throw new ValidacaoException("O clube visitante venceu, logo seu resultado deve ser VITORIA e do mandante DERROTA");
        } else if (dados.qtdeGolsClubeMandante() == dados.qtdeGolsClubeVisitante() && dados.resultadoClubeVisitante() != Resultado.EMPATE && dados.resultadoClubeMandante() != Resultado.EMPATE) {
            throw new ValidacaoException("Houve um empate, logo ambos os resultados devem ser EMPATE");
        } else if (dados.nomeClubeMandante() == dados.nomeClubeVisitante()) {
            throw new ValidacaoException("Você não pode cadastrar  um único clube para partida");
        }

        var partida = validarId(id);
        partida.atualizarInformacoes(id, dados);
        return partida;
    }

    public Page<Partida> encontrarClubeId (Long clubeId, Pageable paginacao){
        return repository.findPartidaByClubeId(clubeId, paginacao);
    }

    public Page<Partida> encontrarEstadioId (Long estadioId, Pageable paginacao){
        return  repository.findPartidaByEstadioId(estadioId, paginacao);
    }

    public Page filtrarParams(String clubeNome, String estadioNome, Pageable paginacao) {
        if (clubeNome != null && !clubeNome.isEmpty()) {
            var clube = repository.findByNome(clubeNome)
                    .orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo nome fornecido"));
           var partida = encontrarClubeId(clube.getId(), paginacao);
            return partida.map(DadosDetalhadamentoPartida::new);
        }

        if(estadioNome != null && !estadioNome.isEmpty()) {
            var estadio = repository.findEstadioByNome(estadioNome);
            var partida = encontrarEstadioId(estadio.get().getId(), paginacao);
            return partida.map(DadosDetalhadamentoPartida::new);
        }
        var page = repository.findAll(paginacao).map(DadosDetalhadamentoPartida::new);
        return page;
    }
}
