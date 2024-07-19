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
class ClubeRepositoryTest {

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
    private Clube clubeInativo = new Clube(clubeCadastro("Botafogo", "RJ", "Rio de Janeiro", false));
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
        em.persist(clubeInativo);
        em.persist(estadio);
        em.persist(estadio2);
        em.persist(partida);
        em.persist(partida2);
        em.flush();
    }

    @Test
    @DisplayName("Deveria retornar null quando o id não for encontrado ou quando o perfil estiver inativo")
    void findByIdAndStatusCenario1() {
        Long idTest = clubeInativo.getId();
        var clubeInativo = clubeRepository.findByIdAndStatus(idTest);
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
        Long idTest = -1L;
        var retrospecto = clubeRepository.findRestrospecto(idTest);
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
    @DisplayName("Deveria retornar uma pagina de clube através do seu ID ")
    void findRestrospectoPaginado1() {
        Long idTeste = clube1.getId();

        var listaPartidas = List.of(partida, partida2);
        Page<Partida> paginacaoPartida = new PageImpl<>(listaPartidas);
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Partida> resultadoClubes = clubeRepository.findRestrospectoPaginado(idTeste, paginacao);

        assertThat(resultadoClubes).isNotEmpty();
        assertThat(resultadoClubes.getTotalElements()).isEqualTo(listaPartidas.size());
        assertThat(resultadoClubes.getContent()).containsExactlyElementsOf(paginacaoPartida.getContent());

    }

    @Test
    @DisplayName("Deveria retornar uma lista de retrospecto com o id do clube sendo clube mandante e visitante de todas as partidas")
    void findRestrospectoPaginado2() {
        Long idTeste = 99999L;

        Page<Partida> paginaVazia = Page.empty();
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Partida> resultadoClubes = clubeRepository.findRestrospectoPaginado(idTeste, paginacao);

        assertThat(resultadoClubes).isEmpty();
        assertThat(resultadoClubes.getTotalElements()).isEqualTo(0);
        assertThat(resultadoClubes.getContent()).containsExactlyElementsOf(paginaVazia.getContent());

    }

    @Test
    @DisplayName("Deveria retornar uma pagina com o  clube localizado pelo id")
    void findByNomeContaining1() {
        String clubeNomeTest = clube1.getNome();

        var listaClube = List.of(clube1, clube2, clube3, clubeInativo);
        Page<Clube> paginacaoClube = new PageImpl<>(listaClube);
        Pageable paginacao = PageRequest.of(0, 10);

        Page<Clube> clubetest = clubeRepository.findByNomeContaining(clubeNomeTest, paginacao);
        assertThat(paginacaoClube).isNotEmpty();
        assertThat(paginacaoClube.getTotalElements()).isEqualTo(listaClube.size());
        assertThat(paginacaoClube.getContent()).containsExactlyElementsOf(paginacaoClube.getContent());

    }

    @Test
    @DisplayName("Deveria retornar um clube vazio (null) quando o seu nome não for encontrado")
    void findByNomeContaining2() {
        String nomeTeste = "XXXX";

        Page<Clube> paginaVazia = Page.empty();
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Clube> resultadoClubes = clubeRepository.findByNomeContaining(nomeTeste, paginacao);

        assertThat(resultadoClubes).isEmpty();
        assertThat(resultadoClubes.getTotalElements()).isEqualTo(0);
        assertThat(resultadoClubes.getContent()).containsExactlyElementsOf(paginaVazia.getContent());
    }

    @Test
    @DisplayName("Deveria retornar uma pagina com os clubes que possuem esta sigla de estado")
    void findBySiglaEstadoContaining1() {
        String siglaEstadoTest = clube1.getSiglaEstado();

        var listaClube = List.of(clube1, clube2, clube3, clubeInativo);
        Page<Clube> paginacaoClube = new PageImpl<>(listaClube);
        Pageable paginacao = PageRequest.of(0, 10);

        Page<Clube> clubetest = clubeRepository.findBySiglaEstadoContaining(siglaEstadoTest, paginacao);
        assertThat(clubetest).isNotEmpty();

    }

    @Test
    @DisplayName("Deveria retornar uma pagina vazia quando não há clubes com a sigla de estado inserida")
    void findBySiglaEstadoContaining2() {
        String siglaEstadoTest = "XXXX";

        Page<Clube> paginaVazia = Page.empty();
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Clube> resultadoClubes = clubeRepository.findBySiglaEstadoContaining(siglaEstadoTest, paginacao);

        assertThat(resultadoClubes).isEmpty();
        assertThat(resultadoClubes.getTotalElements()).isEqualTo(0);
        assertThat(resultadoClubes.getContent()).containsExactlyElementsOf(paginaVazia.getContent());
    }

    @Test
    @DisplayName("Deveria retornar uma pagina com os clubes que possuem este local sede")
    void findByLocalSede1() {
        String localSedeTest = clube1.getLocalSede();

        var listaClube = List.of(clube1, clube2, clube3, clubeInativo);
        Page<Clube> paginacaoClube = new PageImpl<>(listaClube);
        Pageable paginacao = PageRequest.of(0, 10);

        Page<Clube> clubetest = clubeRepository.findByLocalSede(localSedeTest, paginacao);
        assertThat(clubetest).isNotEmpty();

    }

    @Test
    @DisplayName("Deveria retornar uma pagina vazia quando não há clubes com o local sede inserido")
    void findByLocalSede2() {
        String localSedeTest = "XXXX";

        Page<Clube> paginaVazia = Page.empty();
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Clube> resultadoClubes = clubeRepository.findByLocalSede(localSedeTest, paginacao);

        assertThat(resultadoClubes).isEmpty();
        assertThat(resultadoClubes.getTotalElements()).isEqualTo(0);
        assertThat(resultadoClubes.getContent()).containsExactlyElementsOf(paginaVazia.getContent());
    }

    @Test
    @DisplayName("Deveria retornar uma pagina com os clubes com status ativo - true")
    void findByStatus1() {
        Boolean statusTest = clube1.getStatus();

        var listaClube = List.of(clube1, clube2, clube3, clubeInativo);
        Page<Clube> paginacaoClube = new PageImpl<>(listaClube);
        Pageable paginacao = PageRequest.of(0, 10);

        Page<Clube> clubetest = clubeRepository.findByStatus(statusTest, paginacao);
        assertThat(clubetest).isNotEmpty();
    }

    @Test
    @DisplayName("Deveria retornar uma pagina com os clubes com status inativo - false")
    void findByStatus2() {
        Boolean statusTest = clubeInativo.getStatus();

        var listaClube = List.of(clube1, clube2, clube3, clubeInativo);
        Page<Clube> paginacaoClube = new PageImpl<>(listaClube);
        Pageable paginacao = PageRequest.of(0, 10);

        Page<Clube> clubetest = clubeRepository.findByStatus(statusTest, paginacao);
        assertThat(clubetest).isNotEmpty();
    }


    @Test
    @DisplayName("Deveria retornar uma pagina com os clubes que possuem esta sigla de estado")
    void existsByNomeAndSiglaEstado1() {
        String clubeNomeTest = clube1.getNome();
        String siglaEstadoTest = clube1.getSiglaEstado();

        Boolean clubetest = clubeRepository.existsByNomeAndSiglaEstado(clubeNomeTest, siglaEstadoTest);
        assertThat(clubetest).isTrue();

    }

    @Test
    @DisplayName("Deveria retornar uma pagina vazia quando não há clubes com a sigla de estado inserida")
    void existsByNomeAndSiglaEstado2() {
        String clubeNomeTest = "XXXX";
        String siglaEstadoTest = "XXXX";

        Boolean clubetest = clubeRepository.existsByNomeAndSiglaEstado(clubeNomeTest, siglaEstadoTest);
        assertThat(clubetest).isFalse();
    }

    @Test
    @DisplayName("Deveria retornar TRUE quando é passada uma data de criação posterio a alguma data da partida")
    void existsPartidasByClubeIdAndDataBefore1() {
        Long clubeId = clube1.getId();
        LocalDateTime now = LocalDateTime.now();
        var existeData = clubeRepository.existsPartidasByClubeIdAndDataBefore(clubeId, now);
        assertThat(existeData).isTrue();
    }

    @Test
    @DisplayName("Deveria retornar FALSE quando é passada uma data de criação anterior a alguma data da partida")
    void existsPartidasByClubeIdAndDataBefore2() {
        Long clubeId = clube1.getId();
        LocalDateTime now = LocalDateTime.now().minusYears(1L);
        var existeData = clubeRepository.existsPartidasByClubeIdAndDataBefore(clubeId, now);
        assertThat(existeData).isFalse();
    }

}