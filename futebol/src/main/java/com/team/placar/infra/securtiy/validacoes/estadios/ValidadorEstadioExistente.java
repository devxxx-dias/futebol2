package com.team.placar.infra.securtiy.validacoes.estadios;

import com.team.placar.domain.estadio.DadosCadastroEstadio;
import com.team.placar.domain.estadio.EstadioRepository;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorEstadioExistente implements ValidadorEstadio {
    @Autowired
    private EstadioRepository repository;

    @Override
    public void validar(DadosCadastroEstadio dados) {

        var estadioExiste = repository.findByNomeIgnoreCase(dados.nome());

        if(estadioExiste != null){
            throw new ConflitException("Já existe um estadio cadastrado com este nome");
        }


    }
}
