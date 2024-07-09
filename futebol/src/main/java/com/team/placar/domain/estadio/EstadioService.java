package com.team.placar.domain.estadio;

import com.team.placar.infra.securtiy.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EstadioService {
    @Autowired
    private EstadioRepository repository;


    public Estadio salvar(DadosCadastroEstadio dados) {
        var estadio = new Estadio(dados);
        repository.save(estadio);
        return estadio;
    }

    public Estadio atualizar(DadosAtualizacaoEstadio dados, Long id) {
        var estadio = repository.findById(id).orElseThrow(() -> new ValidacaoException("Estádio não localizado pelo id"));
        estadio.atualizarInformacoes(dados);
        return estadio;
    }

    public Estadio buscar(Long id) {
        var estadio = repository.findById(id).orElseThrow(() -> new ValidacaoException("Estádio não localizado pelo id"));
        return estadio;
    }
}
