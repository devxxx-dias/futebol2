package com.team.placar.domain.estadio;

import com.team.placar.domain.clube.Clube;
import com.team.placar.domain.clube.ClubeRepository;
import com.team.placar.domain.clube.DadosClubeCadastro;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EstadioRepositoryTest {


    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    PartidaRepository partidaRepository;

    @Autowired
    EstadioRepository estadioRepository;

    @Autowired
    TestEntityManager em;


    private final LocalDateTime dataCriacaoTime = LocalDateTime.now().minusMinutes(5L);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final LocalDate dataCriacao = LocalDate.parse("02-02-2022", formatter);
    private final Clube clube1 = new Clube(clubeCadastro("Palmeiras", "SP", "São Paulo", true));
    private final Clube clube2 = new Clube(clubeCadastro("São Paulo", "SP", "São Paulo", true));
    private final Clube clube3 = new Clube(clubeCadastro("Flamengo", "RJ", "Rio de Janeiro", true));
    private final Clube clube4 = new Clube(clubeCadastro("Botafogo", "RJ", "Rio de Janeiro", true));
    private final Estadio estadio = new Estadio(null, "Pacaembu", "São Paulo", "SP");
    private final Estadio estadio2 = new Estadio(null, "Maracanã", "Rio de Janeiro", "RJ");
    private final Partida partida = new Partida(null, clube1, clube2, estadio, 10, 5, Resultado.VITORIA, Resultado.DERROTA, dataCriacaoTime);
    private final Partida partida2 = new Partida(null, clube1, clube2, estadio, 10, 5, Resultado.VITORIA, Resultado.DERROTA, dataCriacaoTime);


    private DadosClubeCadastro clubeCadastro(String nome, String siglaEstado, String localSede, Boolean status) {
        return new DadosClubeCadastro(
                nome,
                siglaEstado,
                localSede,
                dataCriacao,
                status
        );

    }


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
        em.flush();
    }


    @Test
    @DisplayName("Deveria retornar nulo quando o nome não for encontrado ou quando o perfil estiver inativo")
    void findByNomeIgnoreCaseCenario1() {
        String nome = "XXX";
        Estadio estadioEncontrado = estadioRepository.findByNomeIgnoreCase(nome);
        assertThat(estadioEncontrado).isNull();
    }

    @Test
    @DisplayName("Deveria retornar true quando o nome for encontrado e o perfil estiver ativo")
    void findByNomeIgnoreCaseCenario2() {
        String nome = estadio.getNome();
        Estadio estadioEncontrado = estadioRepository.findByNomeIgnoreCase(nome);
        assertThat(estadioEncontrado).isEqualTo(estadio);
    }

    @Test
    @DisplayName("Deveria retornar false quando o nome for encontrado mas  pertencer ao  outro clube do id indicado")
    void existsClubeByIdIsNotAndNome() {
        Long idClube = clube1.getId();
        String nomeOutroClube = clube2.getNome();
        var checarNomeDiferenteId = estadioRepository.existsClubeByIdIsNotAndNome(idClube, nomeOutroClube);
        assertThat(checarNomeDiferenteId).isFalse();
    }

    @Test
    @DisplayName("Deveria retornar true quando o nome for encontrado pertencer ao outro clube diferente do id indicado")
    void existsClubeByIdIsNotAndNome2() {
        Estadio estadio2 = new Estadio(null, "Maracanã", "Rio de Janeiro", "RJ");
        em.persist(estadio2);
        Estadio estadio3 = new Estadio(null, "Maracanã", "São Paulo", "SP");
        em.persist(estadio3);
        em.flush(); // Ensure both entities are persisted to the database


        boolean checarNomeDiferenteId = estadioRepository.existsClubeByIdIsNotAndNome(estadio3.getId(), estadio2.getNome());


        assertThat(checarNomeDiferenteId).isTrue();
    }

}