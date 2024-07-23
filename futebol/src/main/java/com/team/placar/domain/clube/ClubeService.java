package com.team.placar.domain.clube;


import com.team.placar.domain.partida.DadosDetalhadamentoPartida;
import com.team.placar.domain.partida.Partida;
import com.team.placar.domain.partida.PartidaRepository;
import com.team.placar.domain.partida.Resultado;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ClubeService {

    @Autowired
    private ClubeRepository repository;

    @Autowired
    private PartidaRepository partidaRepository;


    public Clube salvar(DadosClubeCadastro dados) {
        validarNomeEstado(dados);
        var clube = new Clube(dados);
        clube = repository.save(clube);
        return clube;
    }

    public Clube buscar(Long id) {
        var clube = repository.findByIdAndStatus(id).orElseThrow(() -> new EntityNotFoundException("Clube não encontrado pelo id ou não está ativo"));

        return clube;
    }

    public void deletar(Long id) {
        var clube = repository.findByIdAndStatus(id).orElseThrow(() -> new EntityNotFoundException("Clube não encontrado pelo id ou não está ativo"));

        clube.deletar();
        repository.save(clube);
    }

    public Clube atualizar(Long id, DadosClubeCadastro dados) {
        var clube = repository.findById(id).orElseThrow(() -> new ValidacaoException("Clube não encontrado pelo id ou não está ativo"));
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

    public void checarDataCriacaoPorPartida(Long id, DadosClubeCadastro dados) {
        LocalDateTime dataCriacaoInicioDoDia = dados.dataCriacao().atStartOfDay();
        var existe = repository.existsPartidasByClubeIdAndDataBefore(id, dataCriacaoInicioDoDia);
        if (existe) {
            throw new ConflitException("Não é possível cadastrar uma data depois da data de uma partida do clube");
        }
    }

    public Page filtrarParams(String nome, String siglaEstado, String localSede, String status, Pageable paginacao) {

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
        var clube = buscar(id);
        var totalVitorias = 0;
        var totalDerrotas = 0;
        var totalEmpates = 0;
        var totalGolsFeito = 0;
        var totalGolsSofridos = 0;

        var retrospectiva = repository.findRestrospecto(id);

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

        DadosRestropctoClubeDetalhadamento dados = new DadosRestropctoClubeDetalhadamento(clube.getNome(), totalVitorias, totalDerrotas, totalEmpates, totalGolsFeito, totalGolsSofridos);

        return dados;
    }


    public DadosRestrospctoClubeAdversarioDto efeituarRestrospectivaAdversario(Long idClube, Long idClubeAdversario) {
        var clubeSelecionado = buscar(idClube);
        var clubeAdversario = buscar(idClubeAdversario);

        List<Partida> retrospectiva = partidaRepository.findRestrospecto(idClube, idClubeAdversario);

        int totalVitorias = 0;
        int totalDerrotas = 0;
        int totalEmpates = 0;
        int totalGolsFeito = 0;
        int totalGolsSofridos = 0;

        for (var retro : retrospectiva) {
            if ((retro.getResultadoClubeMandante().equals(Resultado.VITORIA) && retro.getClubeMandante().getId().equals(idClube)) ||
                    (retro.getResultadoClubeVisitante().equals(Resultado.VITORIA) && retro.getClubeVisitante().getId().equals(idClube))) {
                totalVitorias++;
            } else if ((retro.getResultadoClubeMandante().equals(Resultado.DERROTA) && retro.getClubeMandante().getId().equals(idClube)) ||
                    (retro.getResultadoClubeVisitante().equals(Resultado.DERROTA) && retro.getClubeVisitante().getId().equals(idClube))) {
                totalDerrotas++;
            } else if (retro.getResultadoClubeMandante().equals(Resultado.EMPATE) && retro.getResultadoClubeVisitante().equals(Resultado.EMPATE)) {
                totalEmpates++;
            }

            if (retro.getClubeMandante().getId().equals(idClube)) {
                totalGolsFeito += retro.getQtdeGolsClubeMandante();
                totalGolsSofridos += retro.getQtdeGolsClubeVisitante();
            } else {
                totalGolsFeito += retro.getQtdeGolsClubeVisitante();
                totalGolsSofridos += retro.getQtdeGolsClubeMandante();
            }
        }

        return new DadosRestrospctoClubeAdversarioDto(
                clubeSelecionado.getNome(),
                clubeAdversario.getNome(),
                totalVitorias,
                totalDerrotas,
                totalEmpates,
                totalGolsFeito,
                totalGolsSofridos
        );
    }
    //TODO realizar os testes unitários e adaptação dos novas implementacoes nos teste tambeém
    //Filtro Avancado
    public Page<Detalhadamento> filtrarBuscar(Long id, String atuouComo, Pageable paginacao) {
        Page<Detalhadamento> page = null;
        System.out.println();
        if (atuouComo != null && !atuouComo.isEmpty()) {
            switch (atuouComo.toLowerCase()) {
                case "mandante":
                    return page = repository.findByClubeMandante(id, paginacao).map(DadosDetalhadamentoPartida::new);
                case "visitante":
                    return page = repository.findByClubeVisitante(id, paginacao).map(DadosDetalhadamentoPartida::new);
                default:
                    return page = new PageImpl<>(Collections.emptyList(), paginacao, 0);
            }
        }

         page = repository.findByIdPage(id, paginacao).map(DadosClubeDetalhadamento::new);

        if (page.isEmpty()) {
            throw new EntityNotFoundException("Clube não encontrado pelo ID fornecido");
        }

        return page;
    }

}