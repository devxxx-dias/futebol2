package com.team.placar.domain.clube;


import com.team.placar.domain.partida.Partida;
import com.team.placar.domain.partida.Resultado;
import com.team.placar.infra.securtiy.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ClubeService {

    @Autowired
    private ClubeRepository repository;

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
        var clube = repository.findById(id)
                .orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo id ou não está ativo"));
        clube.atualizarInformacoes(dados);
        repository.save(clube);
        return clube;
    }

    public Clube validarId(Long id) {
        var clube = repository.findById(id).orElseThrow(() -> new ValidacaoException("Partida não encontrada pelo ID"));
        return clube;
    }

    public Page filtrarParams(
            String nome,
            String siglaEstado,
            String localSede,
            String status,
            Pageable paginacao
    ) {
        if (nome != null && !nome.isEmpty()) {
            return findByNome(nome, paginacao);
        }
        if(siglaEstado != null && !siglaEstado.isEmpty()) {
            return findBySiglaEstado(siglaEstado, paginacao);
        }
        if(localSede != null && !localSede.isEmpty()) {
            return findByLocalSede(localSede, paginacao);
        }
        if(status != null) {
            return findByStatus(status, paginacao);
        }

        var page = repository.findAll(paginacao).map(DadosClubeDetalhadamento::new);
        return page;
    }

    public Page<Clube> findByNome(String nome, Pageable pageable) {
        return repository.findByNomeContaining(nome, pageable);
    }

    public Page<Clube> findBySiglaEstado(String siglaEstado, Pageable pageable){
        return repository.findBySiglaEstadoContaining(siglaEstado, pageable);
    }

    public Page<Clube>findByLocalSede(String localSede, Pageable pageable){
        return  repository.findByLocalSede(localSede, pageable);
    }

    public Page<Clube>findByStatus(String getStatus, Pageable pageable){
    var status = false;
        if(getStatus.equalsIgnoreCase("ativo") || getStatus.equalsIgnoreCase("ativa")){
          status = true;
        }
        if(getStatus.equalsIgnoreCase("inativo") || getStatus.equalsIgnoreCase("inativa")){
          status=false;
        }
        return  repository.findByStatus(status,pageable);
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
