package com.team.placar.domain.clube;


import com.team.placar.domain.partida.Partida;
import com.team.placar.domain.partida.Resultado;
import com.team.placar.infra.securtiy.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ClubeService {

    @Autowired
    private ClubeRepository repository;

    //para o endpoint listar um clube
    public Clube buscar(Long id) {
        var clube = repository.findByIdAndStatus(id)
                .orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo id ou não está ativo"));

        return clube;
    }


    public Clube salvar(DadosClubeCadastro dados) {
        var clube = new Clube(dados);
        clube = repository.save(clube);
        return clube;
    }

    public void deletar(Long id) {
        var clube = repository.findByIdAndStatus(id)
                .orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo id ou não está ativo"));

        clube.deletar();
        repository.save(clube);
    }

    public Clube validar(Long id, DadosClubeCadastro dados) {
        var clube = repository.findByIdAndStatus(id)
                .orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo id ou não está ativo"));
        clube.atualizarInformacoes(dados);
        repository.save(clube);
        return clube;
    }

    public Clube validarId(Long id) {
        var clube = repository.findById(id).orElseThrow(() -> new ValidacaoException("Partida não encontrada pelo ID"));
        return clube;
    }

    public DadosRestropctoClubeDetalhadamento efeituarRestropctiva(Long id) {
        var retrospectiva = repository.findRestrospecto(id);
        retrospectiva.forEach(System.out::println);
        var clube = validarId(id);
        var totalVitorias = 0;
        var totalDerrotas = 0;
        var totalEmpates = 0;
        var totalGolsFeito = 0;
        var totalGolsSofridos = 0;

        for (var retro : retrospectiva) {
            if (retro.getResultadoClubeMandante().equals(Resultado.VITORIA)) {
                totalVitorias++;
            }
            if (retro.getResultadoClubeMandante().equals(Resultado.DERROTA)) {
                totalDerrotas++;
            }
            if (retro.getResultadoClubeMandante().equals(Resultado.EMPATE)) {
                totalEmpates++;
            }
            totalGolsFeito += retro.getQtdeGolsClubeMandante();
            totalGolsSofridos += retro.getQtdeGolsClubeVisitante();
        }

        DadosRestropctoClubeDetalhadamento dados = new DadosRestropctoClubeDetalhadamento(clube.getNome(), totalVitorias, totalDerrotas, totalEmpates,
                totalGolsFeito, totalGolsSofridos);

        return dados;
    }

    public Page<Partida> listarRestrospectivaId(Long id, Pageable pageable) {
        var retrospectiva = repository.findRestrospectoPaginado(id, pageable);
        return retrospectiva;
    }

}
