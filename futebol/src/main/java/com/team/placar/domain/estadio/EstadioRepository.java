package com.team.placar.domain.estadio;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadioRepository extends JpaRepository<Estadio,Long> {

    Boolean findByNomeIgnoreCase(String nome);

    Boolean existsClubeByIdIsNotAndNome(Long id, String nome);
}
