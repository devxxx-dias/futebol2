package com.team.placar.domain.partida;

import com.team.placar.domain.clube.Clube;
import com.team.placar.domain.estadio.Estadio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    @Query("SELECT c FROM Clube c WHERE LOWER(c.nome) = LOWER(:nome) AND c.status = true")
    Optional<Clube> findByNome(String nome);

    @Query("SELECT c FROM Clube c WHERE LOWER(c.nome) = LOWER(:nome) AND c.status = true")
    Clube findClube (String nome);

    @Query("SELECT e FROM Estadio e WHERE LOWER(e.nome) = LOWER(:nome)")
    Optional<Estadio> findEstadioByNome( String nome);

    @Query("SELECT p FROM partida p WHERE p.clubeMandante.id = :id OR p.clubeVisitante.id = :id")
    Page<Partida> findPartidaByClubeId(Long id, Pageable pageable);

    @Query("SELECT  p FROM partida p WHERE p.estadio.id = :id")
    Page<Partida> findPartidaByEstadioId(Long id, Pageable paginacao);

    @Query("""
        SELECT COUNT(p) > 0
        FROM partida p
        WHERE (p.clubeMandante.id = :clubeId OR p.clubeVisitante.id = :clubeId)
        AND :dataPartida < (SELECT c.dataCriacao FROM Clube c WHERE c.id = :clubeId)
        """)
    boolean existsPartidasByClubeIdAndDataBefore( Long clubeId,  LocalDate dataPartida);

    @Query("""
        SELECT COUNT(p) > 0
        FROM partida p
        WHERE (p.clubeMandante.id = :clubeId OR p.clubeVisitante.id = :clubeId)
        AND ABS(TIMESTAMPDIFF(HOUR , p.dataHora, :dataPartida)) < 48
        """)
    boolean existsPartidasWithin48Hours(Long clubeId, LocalDateTime dataPartida);


    boolean existsPartidaByEstadio_IdAndDataHoraBetween(Long estadioId, LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim);

}
