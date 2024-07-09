package com.team.placar.domain.partida;

import com.team.placar.domain.clube.Clube;
import com.team.placar.infra.securtiy.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
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
        } else if (dados.qtdeGolsClubeMandante() == dados.qtdeGolsClubeVisitante()  && dados.resultadoClubeVisitante() != Resultado.EMPATE && dados.resultadoClubeMandante() != Resultado.EMPATE) {
            throw new ValidacaoException("Houve um empate, logo ambos os resultados devem ser EMPATE");
        } else if (dados.nomeClubeMandante() == dados.nomeClubeVisitante()) {
            throw new ValidacaoException("Você não pode cadastrar  um único clube para partida");
        }

        var clubeMandante = repository.findByNome(dados.nomeClubeMandante()).orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo nome ou não está ativo"));
        var clubeVisitante = repository.findByNome(dados.nomeClubeVisitante()).orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo nome ou não está ativo"));
        var partida = new Partida(clubeMandante, clubeVisitante, dados.qtdeGolsClubeMandante(), dados.qtdeGolsClubeVisitante(), dados.resultadoClubeMandante(), dados.resultadoClubeVisitante(), dados.dataHora());
        repository.save(partida);
        return partida;
    }


    public Partida validarDadosID(DadosAtualizarPartida dados, Long id) {
        if (dados.qtdeGolsClubeMandante() > dados.qtdeGolsClubeVisitante() && dados.resultadoClubeMandante() != Resultado.VITORIA) {
            throw new ValidacaoException("O clube mandante venceu, logo seu resultado deve ser VITORIA e do visitante DERROTA");
        } else if (dados.qtdeGolsClubeVisitante() > dados.qtdeGolsClubeMandante() && dados.resultadoClubeVisitante() != Resultado.VITORIA) {
            throw new ValidacaoException("O clube visitante venceu, logo seu resultado deve ser VITORIA e do mandante DERROTA");
        } else if (dados.qtdeGolsClubeMandante() == dados.qtdeGolsClubeVisitante()  && dados.resultadoClubeVisitante() != Resultado.EMPATE && dados.resultadoClubeMandante() != Resultado.EMPATE) {
            throw new ValidacaoException("Houve um empate, logo ambos os resultados devem ser EMPATE");
        } else if (dados.nomeClubeMandante() == dados.nomeClubeVisitante()) {
            throw new ValidacaoException("Você não pode cadastrar  um único clube para partida");
        }

        var partida = repository.findById(id).orElseThrow(() -> new ValidacaoException("Partida não encontrada pelo ID"));
        partida.atualizarInformacoes(id, dados);
        return partida;
    }

    public Partida validarId(Long id) {
        var partida = repository.findById(id).orElseThrow(() -> new ValidacaoException("Partida não encontrada pelo ID"));
        return partida;
    }
}
