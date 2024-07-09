package com.team.placar.domain.estadio;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "Estadio")
@Table(name = "estadios")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Estadio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String cidade;
    private String siglaEstado;

    public Estadio(DadosCadastroEstadio dados) {
        this(null,dados.nome(),dados.cidade(),dados.siglaEstado());
    }

    public void atualizarInformacoes(DadosAtualizacaoEstadio dados) {
        if(dados.nome() != null){
            this.nome = dados.nome();
        }
        if(dados.cidade() != null){
            this.cidade = dados.cidade();
        }
        if (dados.siglaEstado() != null){
            this.siglaEstado = dados.siglaEstado();
        }
    }
}

//Inicio do mapeamento do banco  e termino dos endpoints estadios
