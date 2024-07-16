package com.team.placar.domain.estadio;

import com.team.placar.domain.clube.DadosClubeCadastro;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import com.team.placar.infra.securtiy.validacoes.estadios.ValidadorEstadio;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadioService {
    @Autowired
    private EstadioRepository repository;

    @Autowired
    private List<ValidadorEstadio> validadores;

    public Estadio salvar(DadosCadastroEstadio dados) {
        validadores.forEach(v -> v.validar(dados));
        var estadio = new Estadio(dados);
        repository.save(estadio);
        return estadio;
    }

    public Estadio atualizar(DadosCadastroEstadio dados, Long id) {
        var estadio = buscarId(id);
        validarNomeEstadoPeloId(id, dados);
        estadio.atualizarInformacoes(dados);
        return estadio;
    }

    public Estadio buscarId(Long id) {
        var estadio = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Estádio não localizado pelo id"));
        return estadio;
    }

    public void validarNomeEstadoPeloId(Long id, DadosCadastroEstadio dados) {
        var existe = repository.existsClubeByIdIsNotAndNome(id, dados.nome());
        if (existe) {
            throw new ConflitException("Já existe um estadio cadastrado com esse nome");
        }
    }

}
