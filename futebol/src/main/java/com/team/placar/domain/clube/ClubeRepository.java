package com.team.placar.domain.clube;


import com.team.placar.domain.partida.DadosDetalhadamentoPartida;
import com.team.placar.domain.partida.Partida;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClubeRepository  extends JpaRepository<Clube, Long> {
    @Query("SELECT c FROM Clube c WHERE c.id = :id AND c.status = true")
    Optional<Clube> findByIdAndStatus( Long id);

    @Query("SELECT p FROM partida p WHERE p.clubeMandante.id = :id OR p.clubeVisitante.id = :id")
    List<Partida> findRestrospecto( Long id);

    @Query("SELECT p FROM partida p WHERE p.clubeMandante.id = :id OR p.clubeVisitante.id = :id")
    Page<Partida> findRestrospectoPaginado(Long id, Pageable pageable);

    Page<Clube> findByNomeContaining(String nome, Pageable pageable);

    Page<Clube> findBySiglaEstadoContaining(String siglaEstado, Pageable pageable);

    Page<Clube> findByLocalSede(String localSede, Pageable pageable);

    Page<Clube> findByStatus(Boolean status, Pageable pageable);

    Boolean existsByNomeAndSiglaEstado(String nome, String siglaEstado);

    Boolean existsClubeByIdIsNotAndNomeAndAndSiglaEstado(Long id, String nome, String siglaEstado);

        @Query("SELECT COUNT(p) > 0 FROM partida p " +
                "WHERE (p.clubeMandante.id = :clubeId OR p.clubeVisitante.id = :clubeId) " +
                "AND p.dataHora < :dataCriacao")
        boolean existsPartidasByClubeIdAndDataBefore( Long clubeId,  LocalDateTime dataCriacao);


    @Query("SELECT p FROM partida p WHERE p.clubeMandante.id = :clubeId")
    Page<Partida> findByClubeMandante(Long clubeId, Pageable pageable);

    @Query("SELECT p FROM partida p WHERE p.clubeVisitante.id = :clubeId")
    Page<Partida> findByClubeVisitante(@Param("clubeId") Long clubeId, Pageable pageable);

    @Query("SELECT c FROM Clube c WHERE c.id = :id AND c.status = true")
    Page<Clube> findByIdPage(Long id, Pageable pageable);
}



