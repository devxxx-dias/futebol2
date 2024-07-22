package com.team.placar.domain.partida;

import com.team.placar.domain.clube.Clube;
import com.team.placar.domain.clube.ClubeRepository;
import com.team.placar.domain.clube.DadosClubeCadastro;
import com.team.placar.domain.clube.DadosClubeDetalhadamento;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class PartidaRepositoryTest {


    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    PartidaRepository partidaRepository;

    @Autowired
    EstadioRepository estadioRepository;

    @Autowired
    TestEntityManager em;


    private LocalDateTime dataCriacaoTime = LocalDateTime.now().minusMinutes(5L);
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private LocalDate dataCriacao = LocalDate.parse("02-02-2022", formatter);
    private Clube clube1 = new Clube(clubeCadastro("Palmeiras", "SP", "São Paulo", true));
    private Clube clube2 = new Clube(clubeCadastro("São Paulo", "SP", "São Paulo", true));
    private Clube clube3 = new Clube(clubeCadastro("Flamengo", "RJ", "Rio de Janeiro", true));
    private Clube clube4 = new Clube(clubeCadastro("Botafogo", "RJ", "Rio de Janeiro", true));
    private Estadio estadio = new Estadio(null, "Pacaembu", "São Paulo", "SP");
    private Estadio estadio2 = new Estadio(null, "Maracanã", "Rio de Janeiro", "RJ");
    private Partida partida = new Partida(null, clube1, clube2, estadio, 10, 5, Resultado.VITORIA, Resultado.DERROTA, dataCriacaoTime);
    private Partida partida2 = new Partida(null, clube1, clube2, estadio, 10, 5, Resultado.VITORIA, Resultado.DERROTA, dataCriacaoTime);


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
    @DisplayName("Deveria retornar um clube quando o seu nome for inserido")
    void findByNomeCenario1() {
        String clubeNomeTest = clube1.getNome();
        Optional<Clube> clube = partidaRepository.findByNome(clubeNomeTest);
        assertThat(clube).isPresent().hasValue(clube1);
    }

    @Test
    @DisplayName("Deveria retornar um clube vazio (null) quando o seu nome não for encontrado")
    void findByNomeCenario2() {
        String nomeTeste = "XXXX";
        Optional<Clube> clube = partidaRepository.findByNome(nomeTeste);
        assertThat(clube).isEmpty();
    }


    @Test
    @DisplayName("Deveria retornar um estadio quando o seu nome for inserido")
    void findEstadioByNomeCenario1() {
        String estadioNomeTest = estadio.getNome();
        Optional<Estadio> estadiotest = partidaRepository.findEstadioByNome(estadioNomeTest);
        assertThat(estadiotest).isPresent();
        assertThat(estadiotest.get()).isEqualTo(estadio);
    }

    @Test
    @DisplayName("Deveria retornar um estadio vazio (null) quando o seu nome não for encontrado")
    void findEstadioByNomeCenario2() {
        String estadioNomeTest = "XXXX";
        Optional<Estadio> estadiotest = partidaRepository.findEstadioByNome(estadioNomeTest);
        assertThat(estadiotest).isEmpty();
    }

    @Test
    @DisplayName("Deveria retornar uma pagina com as partidas de um clube através do seu ID ")
    void findPartidaByClubeIdCenario1() {
        Long idTeste = clube1.getId();
        var listaPartidas = List.of(partida, partida2);
        Page<Partida> paginacaoPartida = new PageImpl<>(listaPartidas);
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Partida> resultadoClubes = partidaRepository.findPartidaByClubeId(idTeste, paginacao);

        assertThat(resultadoClubes).isNotEmpty();
        assertThat(resultadoClubes.getTotalElements()).isEqualTo(listaPartidas.size());

        // Compare the contents instead of the Page objects
        assertThat(resultadoClubes.getContent()).containsExactlyElementsOf(paginacaoPartida.getContent());
    }

    @Test
    @DisplayName("Deveria retornar uma pagina vazia quando o ID do clube não for localizado")
    void findPartidaByClubeIdCenario2() {
        Long idTeste = 99999L;

        Page<Partida> paginaVazia = Page.empty();
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Partida> resultadoClubes = partidaRepository.findPartidaByClubeId(idTeste, paginacao);

        assertThat(resultadoClubes).isEmpty();
        assertThat(resultadoClubes.getTotalElements()).isEqualTo(0);
        assertThat(resultadoClubes.getContent()).containsExactlyElementsOf(paginaVazia.getContent());
    }


    @Test
    @DisplayName("Deveria retornar uma pagina com as partidas de um clube através do seu ID ")
    void findPartidaByEstadioIdCenario1() {
        Long idTeste = estadio.getId();
        var listaPartidas = List.of(partida, partida2);
        Page<Partida> paginacaoPartida = new PageImpl<>(listaPartidas);
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Partida> resultadoClubes = partidaRepository.findPartidaByEstadioId(idTeste, paginacao);

        assertThat(resultadoClubes).isNotEmpty();
        assertThat(resultadoClubes.getTotalElements()).isEqualTo(listaPartidas.size());
        assertThat(resultadoClubes.getContent()).containsExactlyElementsOf(paginacaoPartida.getContent());
    }

    @Test
    @DisplayName("Deveria retornar uma pagina vazia quando o ID do clube não for localizado")
    void findPartidaByEstadioIdCenario2() {
        Long idTeste = 99999L;

        Page<Partida> paginaVazia = Page.empty();
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Partida> resultadoClubes = partidaRepository.findPartidaByEstadioId(idTeste, paginacao);

        assertThat(resultadoClubes).isEmpty();
        assertThat(resultadoClubes.getTotalElements()).isEqualTo(0);
        assertThat(resultadoClubes.getContent()).containsExactlyElementsOf(paginaVazia.getContent());
    }

    @Test
    @DisplayName("Deveria retornar TRUE quando é passada uma data de uma partida antes da data de criacao do clube")
    void existsPartidasByClubeIdAndDataBefore_1() {
        Long idTest = clube1.getId();
        var dataTest = clube1.getDataCriacao().minusDays(1L);

        var existeData = partidaRepository.existsPartidasByClubeIdAndDataBefore(idTest, dataTest);
        assertThat(existeData).isTrue();
    }

    @Test
    @DisplayName("Deveria retornar FALSE quando é passada uma data de uma partida for posterio a data de criacao do clube")
    void existsPartidasByClubeIdAndDataBefore_2() {
        Long idTest = clube1.getId();
        var dataTest = clube1.getDataCriacao().plusDays(1L);

        var existeData = partidaRepository.existsPartidasByClubeIdAndDataBefore(idTest, dataTest);
        assertThat(existeData).isFalse();
    }

    @Test
    @DisplayName("Deveria retornar TRUE quando é passada uma data de uma partida dentro de um intervalo de 48 horas")
    void existsPartidasWithin48Hours_1() {
        Long idTest = clube1.getId();
        var dataTest = partida.getDataHora();

        var existeData = partidaRepository.existsPartidasWithin48Hours(idTest, dataTest);
        assertThat(existeData).isTrue();
    }

    @Test
    @DisplayName("Deveria retornar FALSE quando é passada uma data de uma partida maior que 48")
    void existsPartidasWithin48Hours_2() {
        Long idTest = clube1.getId();
        var dataTest = partida.getDataHora().plusHours(49);

        var existeData = partidaRepository.existsPartidasWithin48Hours(idTest, dataTest);
        assertThat(existeData).isFalse();
    }

    @Test
    @DisplayName("Deveria retornar FALSE quando é passada uma data de uma partida menor que 48")
    void existsPartidasWithin48Hours_3() {
        Long idTest = clube1.getId();
        var dataTest = partida.getDataHora().minusHours(49);

        var existeData = partidaRepository.existsPartidasWithin48Hours(idTest, dataTest);
        assertThat(existeData).isFalse();
    }

    @Test
    @DisplayName("Deveria retornar TRUE quando é passada uma data de uma partida onde já tenha uma partida cadastrada no estadio selecionado")
    void existsPartidaByEstadio_IdAndDataHoraBetween_1() {
        Long idTest = estadio.getId();
        LocalDateTime dataHoraPartida = partida.getDataHora();
        LocalDateTime dataHoraInicio = dataHoraPartida.minusHours(12);
        LocalDateTime dataHoraFim = dataHoraPartida.plusHours(12);

        var existeData = partidaRepository.existsPartidaByEstadio_IdAndDataHoraBetween(idTest, dataHoraInicio, dataHoraFim);
        assertThat(existeData).isTrue();
    }

    @Test
    @DisplayName("Deveria retornar FALSE quando é passada uma data de uma partida posterior a data registrada")
    void existsPartidaByEstadio_IdAndDataHoraBetween_2() {
        Long idTest = estadio.getId();
        LocalDateTime dataHoraPartida = partida.getDataHora();
        LocalDateTime dataHoraInicio = dataHoraPartida.plusDays(1L);
        LocalDateTime dataHoraFim = dataHoraPartida.plusDays(2L);

        var existeData = partidaRepository.existsPartidaByEstadio_IdAndDataHoraBetween(idTest, dataHoraInicio, dataHoraFim);
        assertThat(existeData).isFalse();
    }

    @Test
    @DisplayName("Deveria retornar FALSE quando é passada uma data de uma partida posterior a data registrada")
    void existsPartidaByEstadio_IdAndDataHoraBetween_3() {
        Long idTest = estadio.getId();
        LocalDateTime dataHoraPartida = partida.getDataHora();
        LocalDateTime dataHoraInicio = dataHoraPartida.minusDays(1L);
        LocalDateTime dataHoraFim = dataHoraPartida.minusDays(2L);

        var existeData = partidaRepository.existsPartidaByEstadio_IdAndDataHoraBetween(idTest, dataHoraInicio, dataHoraFim);
        assertThat(existeData).isFalse();
    }
}