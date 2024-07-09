package com.team.placar.domain.partida;

import com.team.placar.domain.clube.Clube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    @Query("SELECT c FROM Clube c WHERE LOWER(c.nome) = LOWER(:nome)")
    Optional<Clube> findByNome(@Param("nome") String nome);
}
