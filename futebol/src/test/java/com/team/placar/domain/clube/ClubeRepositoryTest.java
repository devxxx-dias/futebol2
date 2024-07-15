package com.team.placar.domain.clube;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team.placar.domain.estadio.Estadio;
import com.team.placar.domain.estadio.EstadioRepository;
import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.Partida;
import com.team.placar.domain.partida.PartidaRepository;
import com.team.placar.domain.partida.Resultado;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ClubeRepositoryTest {

    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    PartidaRepository partidaRepository;

    @Autowired
    EstadioRepository estadioRepository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("Deveria retornar null quando o id não for encontrado ou quando o perfil estiver inativo")
    void findByIdAndStatusCenario1() {
        var clubeInativo = clubeRepository.findByIdAndStatus(-1L);
        assertThat(clubeInativo).isEmpty();

    }

    @Test
    @DisplayName("Deveria retornar um clube quando o id for encontrado e o perfil estiver ativo")
    void findByIdAndStatusCenario2() {
        var clubeativo = cadastrarClube("Palmeiras", "SP", "São Paulo", true);
        var clube = clubeRepository.findByIdAndStatus(clubeativo.getId());
        assertThat(clube).isPresent().hasValue(clubeativo);
    }

    @Test
    @DisplayName("Deveria retornar uma lista vazia quando o id do clube não constar em alguma partida para o retorspecto")
    void findRestrospectoCenario1() {
        var retrospecto = clubeRepository.findRestrospecto(-1L);
        assertThat(retrospecto).isEmpty();
    }

    @Test
    @DisplayName("Deveria retornar uma lista de retrospecto com o id do clube sendo clube mandante e visitante de todas as partidas")
    void findRestrospectoCenario2() {
        Long clubeId = clube1.getId();
        List<Partida> retrospecto = clubeRepository.findRestrospecto(clubeId);

        assertThat(retrospecto).extracting("clubeMandante")
                .allMatch(clube -> ((Clube) clube).getId().equals(clubeId) || ((Partida) clube).getClubeVisitante().getId().equals(clubeId));
    }

    @Test
    @DisplayName("Deveria retornar FALSE quando é passada uma data de criacao futura após a data de uma partida")
    void checarDataCriacaoComDataPartida1() {
        Long clubeId = clube1.getId();
        LocalDateTime data = LocalDateTime.now().plusYears(1L);
        var existeData = clubeRepository.existsPartidasByClubeIdAndDataBefore(clubeId, data);
        assertThat(existeData).isFalse();
    }

    @Test
    @DisplayName("Deveria retornar TRUE quando é passada uma data de criação anterior a alguma data da partida")
    void checarDataCriacaoComDataPartida_1() {
        Long clubeId = clube1.getId();
        LocalDateTime data = dataCriacao.atStartOfDay().minusMonths(10L);
        var existeData = clubeRepository.existsPartidasByClubeIdAndDataBefore(clubeId, data);
        assertThat(existeData).isTrue();
    }








    private DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm");
    private LocalDateTime dataCriacaoTime = LocalDateTime.parse("02-02-2022T12:22", formatterTime);
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private LocalDate dataCriacao = LocalDate.parse("02-02-2022", formatter);
    private Clube clube1 = new Clube(clubeCadastro("Palmeiras", "SP", "São Paulo", true));
    private Clube clube2 = new Clube(clubeCadastro("São Paulo", "SP", "São Paulo", true));
    private Clube clube3 = new Clube(clubeCadastro("Flamengo", "RJ", "Rio de Janeiro", true));
    private Clube clube4 = new Clube(clubeCadastro("Botafogo", "RJ", "Rio de Janeiro", true));
    private Estadio estadio = new Estadio(null, "Pacaembu", "São Paulo", "SP");
    private Estadio estadio2 = new Estadio(null, "Maracanã", "Rio de Janeiro", "RJ");

    @BeforeEach
    public void setUp() {
      var clubeA = clube1;
      var clubeB = clube2;
      var clubeC = clube3;
      var clubeD = clube4;
        em.persist(estadio);
        em.persist(estadio2);
    }

    private Clube cadastrarClube(String nome, String siglaEstado, String localSede, Boolean status) {
        var clube = new Clube(clubeCadastro(nome, siglaEstado, localSede, true));
        em.persist(clube);
        return clube;
    }

    private DadosClubeCadastro clubeCadastro(String nome, String siglaEstado, String localSede, Boolean status) {
        return new DadosClubeCadastro(
                nome,
                siglaEstado,
                localSede,
                dataCriacao,
                status
        );

    }

    private Partida cadastrarPartida(Clube clube1, Clube clube2, Estadio estadio) {

        var partida = new Partida(
                null,
                clube1,
                clube2,
                estadio,
                5,
                0,
                Resultado.VITORIA,
                Resultado.DERROTA,
                dataCriacaoTime);
        em.persist(partida);

        return partida;
    }

    private DadosCadastroPartida partidaCadastro(
            String nomeClubeMandante,
            String nomeClubeVisitante,
            String nomeEstadio,
            Integer qtdeGolsClubeMandante,
            Integer qtdeGolsClubeVisitante,
            Resultado resultadoClubeMandante,
            Resultado resultadoClubeVisitante,
            LocalDateTime dataHora
    ) {
        return new DadosCadastroPartida(
                nomeClubeMandante,
                nomeClubeVisitante,
                nomeEstadio,
                qtdeGolsClubeMandante,
                qtdeGolsClubeVisitante,
                resultadoClubeMandante,
                resultadoClubeVisitante,
                dataHora

        );
    }


}