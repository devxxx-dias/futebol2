package com.team.placar.domain.clube;


import com.team.placar.domain.partida.Partida;
import com.team.placar.domain.partida.Resultado;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClubeService {

    @Autowired
    private ClubeRepository repository;


    public Clube salvar(DadosClubeCadastro dados) {
        validarNomeEstado(dados);
        var clube = new Clube(dados);
        clube = repository.save(clube);
        return clube;
    }

    public Clube buscar(Long id) {
        var clube = repository.findByIdAndStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Clube não encontrado pelo id ou não está ativo"));

        return clube;
    }

    public void deletar(Long id) {
        var clube = repository.findByIdAndStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Clube não encontrado pelo id ou não está ativo"));

        clube.deletar();
        repository.save(clube);
    }

    public Clube atualizar(Long id, DadosClubeCadastro dados) {
        var clube = repository.findById(id)
                .orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo id ou não está ativo"));
        validarNomeEstadoPeloId(id, dados);
        checarDataCriacaoPorPartida(id, dados);
        clube.atualizarInformacoes(dados);
        repository.save(clube);
        return clube;
    }

    public void validarNomeEstado(DadosClubeCadastro dados) {
        var existe = repository.existsByNomeAndSiglaEstado(dados.nome(), dados.siglaEstado());
        if (existe) {
            throw new ConflitException("Já existe um clube cadastrado com esse nome neste estado");
        }
    }

    public void validarNomeEstadoPeloId(Long id, DadosClubeCadastro dados) {
        var existe = repository.existsClubeByIdIsNotAndNomeAndAndSiglaEstado(id, dados.nome(), dados.siglaEstado());
        if (existe) {
            throw new ConflitException("Já existe um clube cadastrado com esse nome neste estado");
        }
    }



    public Clube validarId(Long id) {
        var clube = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Partida não encontrada pelo ID"));
        return clube;
    }

    public void checarDataCriacaoPorPartida(Long id, DadosClubeCadastro dados ){
        LocalDateTime dataCriacaoInicioDoDia = dados.dataCriacao().atStartOfDay();
        var existe = repository.existsPartidasByClubeIdAndDataBefore(id, dataCriacaoInicioDoDia);
        if (existe) {
            throw new ConflitException("Não é possível cadastrar uma data após a data de uma partida do clube");
        }
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

        if (siglaEstado != null && !siglaEstado.isEmpty()) {
            return findBySiglaEstado(siglaEstado, paginacao);
        }
        if (localSede != null && !localSede.isEmpty()) {
            return findByLocalSede(localSede, paginacao);
        }
        if (status != null) {
            return findByStatus(status, paginacao);
        }

        var page = repository.findAll(paginacao).map(DadosClubeDetalhadamento::new);
        return page;
    }

    public Page<Clube> findByNome(String nome, Pageable pageable) {
        return repository.findByNomeContaining(nome, pageable);
    }

    public Page<Clube> findBySiglaEstado(String siglaEstado, Pageable pageable) {
        return repository.findBySiglaEstadoContaining(siglaEstado, pageable);
    }

    public Page<Clube> findByLocalSede(String localSede, Pageable pageable) {
        return repository.findByLocalSede(localSede, pageable);
    }

    public Page<Clube> findByStatus(String getStatus, Pageable pageable) {

        if (getStatus.equalsIgnoreCase("ativo") || getStatus.equalsIgnoreCase("ativa") || getStatus.equalsIgnoreCase("true")) {
            return repository.findByStatus(true, pageable);
        }
        if (getStatus.equalsIgnoreCase("inativo") || getStatus.equalsIgnoreCase("inativa") || getStatus.equalsIgnoreCase("false")) {
            return repository.findByStatus(false, pageable);
        }
        return repository.findByStatus(null, pageable);
    }

    public DadosRestropctoClubeDetalhadamento efeituarRestropctiva(Long id) {
        var retrospectiva = repository.findRestrospecto(id);

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
