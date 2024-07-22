package com.team.placar.infra.securtiy.validacoes.partidas;

import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.PartidaRepository;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorEhClubeMandante implements ValidadorPartida {
    @Autowired
    private PartidaRepository repository;


    @Override
    public void validar(DadosCadastroPartida dados) {
        var clubeMandante = repository.findByNome(dados.nomeClubeMandante())
                .orElseThrow(() -> new ValidacaoException("Clube Mandante não encontrado pelo nome ou não está ativo"));
        var estadio = repository.findEstadioByNome(dados.nomeEstadio())
                .orElseThrow(() -> new ValidacaoException("Estádio não encontrado pelo nome fornecido"));

        if(estadio.getCidade().isEmpty()){
            return;

        }

        if (!clubeMandante.getLocalSede().equals(estadio.getCidade())) {
            throw new ValidacaoException("Verifique se o estádio é do clube da casa (mandante)");
        }

    }

}
