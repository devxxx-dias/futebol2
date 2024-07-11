package com.team.placar.domain.partida;

import com.team.placar.domain.clube.Clube;
import com.team.placar.domain.estadio.Estadio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    @Query("SELECT c FROM Clube c WHERE LOWER(c.nome) = LOWER(:nome) AND c.status = true")
    Optional<Clube> findByNome(@Param("nome") String nome);

    @Query("SELECT e FROM Estadio e WHERE LOWER(e.nome) = LOWER(:nome)")
    Optional<Estadio> findEstadioByNome(@Param("nome") String nome);

    @Query("SELECT p FROM partida p WHERE p.clubeMandante.id = :id OR p.clubeVisitante.id = :id")
    Page<Partida> findPartidaByClubeId(Long id, Pageable pageable);

    @Query("SELECT  p FROM partida p WHERE p.estadio.id = :id")
    Page<Partida> findPartidaByEstadioId(Long id, Pageable paginacao);

    boolean existsByDataHoraBetween(LocalDateTime dataMinIntervalo, LocalDateTime dataMaxIntervalo);

}
