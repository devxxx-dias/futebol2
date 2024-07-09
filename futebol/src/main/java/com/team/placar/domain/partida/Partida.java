package com.team.placar.domain.partida;

import com.team.placar.domain.clube.Clube;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "partida")
@Table(name = "partidas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Partida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clube_mandante_id")
    private Clube clubeMandante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clube_visitante_id")
    private Clube clubeVisitante;

    private Integer qtdeGolsClubeMandante;
    private Integer qtdeGolsClubeVisitante;

    @Enumerated(EnumType.STRING)
    private Resultado resultadoClubeMandante;

    @Enumerated(EnumType.STRING)
    private Resultado resultadoClubeVisitante;

    private LocalDateTime dataHora; // "dataHora": "2024-06-16T18:12:00"

    public Partida(Clube clubeMandante, Clube clubeVisitante, Integer qtdeGolsClubeMandante, Integer qtdeGolsClubeVisitante, Resultado resultadoClubeMandante, Resultado resultadoClubeVisitante, LocalDateTime dataHora) {
        this.clubeMandante = clubeMandante;
        this.clubeVisitante = clubeVisitante;
        this.qtdeGolsClubeMandante = qtdeGolsClubeMandante;
        this.qtdeGolsClubeVisitante = qtdeGolsClubeVisitante;
        this.resultadoClubeMandante = resultadoClubeMandante;
        this.resultadoClubeVisitante = resultadoClubeVisitante;
        this.dataHora = dataHora;
    }


    @Override
    public String toString() {
        return "Partida{" +
                "clubeMandante=" + clubeMandante +
                ", clubeVisitante=" + clubeVisitante +
                ", qtdeGolsClubeMandante=" + qtdeGolsClubeMandante +
                ", qtdeGolsClubeVisitante=" + qtdeGolsClubeVisitante +
                ", resultadoClubeMandante=" + resultadoClubeMandante +
                ", resultadoClubeVisitante=" + resultadoClubeVisitante +
                ", dataHora=" + dataHora +
                '}';
    }

    public void atualizarInformacoes(Long id, DadosAtualizarPartida dados) {
        this.id = id;
        if (dados.nomeClubeMandante() != null) {
            this.clubeMandante.setNome(dados.nomeClubeMandante());
        }
        if (dados.resultadoClubeVisitante() != null) {
            this.clubeVisitante.setNome(dados.nomeClubeVisitante());
        }
        if (dados.qtdeGolsClubeMandante() != null) {
            this.qtdeGolsClubeMandante = dados.qtdeGolsClubeMandante();
        }
        if (dados.qtdeGolsClubeVisitante() != null) {
            this.qtdeGolsClubeVisitante = dados.qtdeGolsClubeVisitante();
        }

        this.resultadoClubeMandante = dados.resultadoClubeMandante();
        this.resultadoClubeVisitante = dados.resultadoClubeVisitante();
        this.dataHora = dados.dataHora();

    }
}
