package com.team.placar.domain.clube;


import com.team.placar.domain.partida.Partida;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ClubeRepository  extends JpaRepository<Clube, Long> {
    @Query("SELECT c FROM Clube c WHERE c.id = :id AND c.status = true")
    Optional<Clube> findByIdAndStatus(@Param("id") Long id);

    @Query("SELECT p FROM partida p WHERE p.clubeMandante.id = :id OR p.clubeVisitante.id = :id")
    List<Partida> findRestrospecto(@Param("id") Long id);

    @Query("SELECT p FROM partida p WHERE p.clubeMandante.id = :id OR p.clubeVisitante.id = :id")
    Page<Partida> findRestrospectoPaginado(@Param("id") Long id, Pageable pageable);

}
