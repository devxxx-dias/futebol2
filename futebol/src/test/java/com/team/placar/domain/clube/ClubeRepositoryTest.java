package com.team.placar.domain.clube;

import com.team.placar.domain.estadio.Estadio;
import com.team.placar.domain.estadio.EstadioRepository;
import com.team.placar.domain.partida.DadosCadastroPartida;
import com.team.placar.domain.partida.Partida;
import com.team.placar.domain.partida.PartidaRepository;
import com.team.placar.domain.partida.Resultado;
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
    @DisplayName("Deveria retornar TRUE quando é passada uma data de criação posterio a alguma data da partida")
    void checarDataCriacaoComDataPartida1() {
        Long clubeId = clube1.getId();
        LocalDateTime now = LocalDateTime.now();
        var existeData = clubeRepository.existsPartidasByClubeIdAndDataBefore(clubeId, now);
        assertThat(existeData).isTrue();
    }

    @Test
    @DisplayName("Deveria retornar FALSE quando é passada uma data de criação anterior a alguma data da partida")
    void checarDataCriacaoComDataPartida1_1() {
        Long clubeId = clube1.getId();
        LocalDateTime now = LocalDateTime.now().minusYears(1L);
        var existeData = clubeRepository.existsPartidasByClubeIdAndDataBefore(clubeId, now);
        assertThat(existeData).isFalse();
    }


    private LocalDateTime dataCriacaoTime = LocalDateTime.now().minusMinutes(5L);
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private LocalDate dataCriacao = LocalDate.parse("02-02-2022", formatter);
    private Clube clube1 = new Clube(clubeCadastro("Palmeiras", "SP", "São Paulo", true));
    private Clube clube2 = new Clube(clubeCadastro("São Paulo", "SP", "São Paulo", true));
    private Clube clube3 = new Clube(clubeCadastro("Flamengo", "RJ", "Rio de Janeiro", true));
    private Clube clube4 = new Clube(clubeCadastro("Botafogo", "RJ", "Rio de Janeiro", true));
    private Estadio estadio = new Estadio(null, "Pacaembu", "São Paulo", "SP");
    private Estadio estadio2 = new Estadio(null, "Maracanã", "Rio de Janeiro", "RJ");
    private Partida partida = new Partida(null,clube1, clube2, estadio,10, 5,Resultado.VITORIA,Resultado.DERROTA, dataCriacaoTime);
    private Partida partida2 = new Partida(null,clube1, clube2, estadio,10, 5,Resultado.VITORIA,Resultado.DERROTA, dataCriacaoTime);


    @BeforeEach
    public void setUp() {

        em.persist(clube1);
        em.persist(clube2);
        em.persist(clube3);
        em.persist(clube4);
        em.persist(estadio);
        em.persist(estadio2);
        em.persist(partida);
        em.persist(partida2);
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