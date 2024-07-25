package com.team.placar.domain.clube;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "Clube")
@Table(name = "clubes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Clube {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String siglaEstado;
    private String localSede;
    private LocalDate dataCriacao;
    private Boolean status;

    public Clube(DadosClubeCadastro dados) {
        this.id = null;
        this.nome = dados.nome();
        this.siglaEstado = dados.siglaEstado();
        this.localSede = dados.localSede();
        this.dataCriacao = dados.dataCriacao();
        this.status = dados.status();

    }

    public void atualizarInformacoes(DadosClubeCadastro dados) {
        if (dados.nome() != null) {
            this.nome = dados.nome();
        }
        if (dados.siglaEstado() != null) {
            this.siglaEstado = dados.siglaEstado();
        }
        if (dados.localSede() != null) {
            this.localSede = dados.localSede();
        }
        if (dados.dataCriacao() != null) {
            this.dataCriacao = dados.dataCriacao();
        }
        if (dados.nome() != null) {
            this.nome = dados.nome();
        }
        if (dados.status() != null) {
            this.status = dados.status();
        }
    }

    public void deletar() {
        this.status = false;
    }

    @Override
    public String toString() {
        return "Clube{" +
                "nome='" + nome + '\'' +
                '}';
    }
}
