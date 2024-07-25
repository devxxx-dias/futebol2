package com.team.placar.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team.placar.domain.clube.Clube;
import com.team.placar.domain.clube.DadosClubeCadastro;
import com.team.placar.domain.estadio.DadosCadastroEstadio;
import com.team.placar.domain.estadio.Estadio;
import com.team.placar.domain.partida.*;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import com.team.placar.infra.securtiy.validacoes.partidas.ValidadorPartida;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class PartidaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PartidaService partidaService;

    @Mock
    private ValidadorPartida validadorClubeRepetido;

    @Mock
    private ValidadorPartida validadorClubesExistentes;

    @Mock
    private ValidadorPartida validadorDerrotaClube;

    @Mock
    private ValidadorPartida validadorEhClubeMandante;

    @Mock
    private ValidadorPartida validadorEmpateNaPartida;

    @Mock
    private ValidadorPartida validadorMesmoHorarioEstadio;

    @Mock
    ValidadorPartida validadorPartidaAntesDataCriacaoClube;

    @Mock
    ValidadorPartida validadorPartidaHorariosProximos;

    @Mock
    ValidadorPartida validadorVitoriaClubeMandante;

    @Mock
    ValidadorPartida validadorVitoriaClubeVisitante;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Ensure PartidaService uses the mocked validators
        when(partidaService.salvar(any(DadosCadastroPartida.class)))
                .thenAnswer(invocation -> {
                    DadosCadastroPartida dados = invocation.getArgument(0);
                    for (ValidadorPartida validador : List.of(
                            validadorClubeRepetido,
                            validadorClubesExistentes,
                            validadorDerrotaClube,
                            validadorEhClubeMandante,
                            validadorEmpateNaPartida,
                            validadorMesmoHorarioEstadio,
                            validadorPartidaAntesDataCriacaoClube,
                            validadorPartidaHorariosProximos,
                            validadorVitoriaClubeMandante,
                            validadorVitoriaClubeVisitante
                    )) {
                        validador.validar(dados);
                    }
                    return new Partida(); // Replace with actual saving logic
                });
    }


    @MockBean
    private PartidaRepository partidaRepository;

    @Autowired
    JacksonTester<DadosCadastroPartida> dadosCadastroPartidaJacksonTester;

    @Autowired
    JacksonTester<DadosDetalhadamentoPartida> dadosDetalhadamentoPartidaJacksonTester;

    @Autowired
    JacksonTester<Page<DadosDetalhadamentoPartida>> dadosDetalhadamentoJacksonTesterPage;

    private LocalDateTime data = LocalDate.of(2002, 2, 22).atStartOfDay();
    private DadosCadastroPartida dadosCadastroPartida = new DadosCadastroPartida("Palmeiras",
            "Flamento",
            "Pacaembu",
            5,
            1,
            Resultado.VITORIA,
            Resultado.DERROTA,
            data
    );


    private LocalDate dataCriacao = LocalDate.of(2002, 2, 22).plusDays(2);
    private Clube clubeMandante = new Clube(new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", dataCriacao, true));
    private Clube clubeVisitante = new Clube(new DadosClubeCadastro("Flamento", "RJ", "Rio de Janeiro", dataCriacao, true));
    private Estadio estadioSp = new Estadio(new DadosCadastroEstadio("Pacaembu", "São Paulo", "SP"));
    private Estadio estadioRj = new Estadio(new DadosCadastroEstadio("Maracana", "Rio de Janeiro", "RJ"));
    private int qtdeGolVitoria = 5;
    private int qtdeGolDerrota = 1;
    private Resultado resultadoVitoria = Resultado.VITORIA;
    private Resultado resultadoDerrota = Resultado.DERROTA;
    private Resultado resultadoEmpate = Resultado.EMPATE;


    private Partida partidaMandanteVitoria = new Partida(
            clubeMandante,
            clubeVisitante,
            estadioSp,
            qtdeGolVitoria,
            qtdeGolDerrota,
            resultadoVitoria,
            resultadoDerrota,
            data);

    private Partida partidaMandanteDerrota = new Partida(
            clubeMandante,
            clubeVisitante,
            estadioRj,
            qtdeGolDerrota,
            qtdeGolVitoria,
            resultadoDerrota,
            resultadoVitoria,
            data);


    private Partida partidaEmpate = new Partida(
            clubeMandante,
            clubeVisitante,
            estadioSp,
            qtdeGolVitoria,
            qtdeGolVitoria,
            resultadoEmpate,
            resultadoEmpate,
            data);

    private DadosDetalhadamentoPartida dadosDetalhadamentoPartida = new DadosDetalhadamentoPartida(partidaMandanteVitoria);

    @Test
    @DisplayName("Deveria devolver codigo http 201 e retornar uma partida for  criada")
    void cadastrarCenario1() throws Exception {

        when(partidaService.salvar(any())).thenReturn(partidaMandanteVitoria);

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        var jsonEsperado = dadosDetalhadamentoPartidaJacksonTester.write(
                dadosDetalhadamentoPartida
        ).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando informacoes estao invalidas")
    void cadastrarCenario2() throws Exception {
        var response = mvc.perform(post("/partida"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do clube MANDANTE não for inserido")
    void cadastrarCenario3() throws Exception {
        String nomeClubeMandante = "";
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                nomeClubeMandante,
                "Flamento",
                "Pacaembu",
                5,
                1,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));


        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nomeClubeMandante","mensagem":"O Nome do clube Mandante deve ser infomado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do clube MANDANTE possuir algum numero inserido")
    void cadastrarCenario3_1() throws Exception {
        String nomeClubeMandante = "Flamengo1";
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                nomeClubeMandante,
                "Flamento",
                "Pacaembu",
                5,
                1,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));


        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nomeClubeMandante","mensagem":"Verifique se há números em algum nome"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do clube VISITANTE não for inserido")
    void cadastrarCenario4() throws Exception {
        String nomeClubeVisitante = "";
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                nomeClubeVisitante,
                "Pacaembu",
                5,
                1,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));


        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nomeClubeVisitante","mensagem":"O Nome do clube Visitante deve ser infomado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do clube VISITANTE possuir algum numero inserido")
    void cadastrarCenario4_1() throws Exception {
        String nomeClubeVisitante = "Flamengo1";
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                nomeClubeVisitante,
                "Pacaembu",
                5,
                1,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));


        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nomeClubeVisitante","mensagem":"Verifique se há números em algum nome"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do ESTADIO não for inserido")
    void cadastrarCenario5() throws Exception {
        String nomeEstadio = "";
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                "São Paulo",
                nomeEstadio,
                5,
                1,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));


        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nomeEstadio","mensagem":"O nome do estádio deve ser inserido"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a QUANTIDADE DE GOLS CLUBE MANDANTE não for inserido")
    void cadastrarCenario6() throws Exception {
        Integer qtdeGolsClubeMandante = null;
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                "São Paulo",
                "Pacaembu",
                qtdeGolsClubeMandante,
                1,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));


        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"qtdeGolsClubeMandante","mensagem":"A quantidade de gols do clube mandante deve ser informado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a QUANTIDADE DE GOLS CLUBE MANDANTE  for um número negativo")
    void cadastrarCenario6_1() throws Exception {
        Integer qtdeGolsClubeMandante = -1;
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                "São Paulo",
                "Pacaembu",
                qtdeGolsClubeMandante,
                1,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));


        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"qtdeGolsClubeMandante","mensagem":"A quantidade de gols do clube mandante não pode ser negativa"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a QUANTIDADE DE GOLS CLUBE VISITANTE não for inserido")
    void cadastrarCenario7() throws Exception {
        Integer qtdeGolsClubeVisitante = null;
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                "São Paulo",
                "Pacaembu",
                1,
                qtdeGolsClubeVisitante,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));


        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"qtdeGolsClubeVisitante","mensagem":"A quantidade de gols do clube visitante deve ser informado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a QUANTIDADE DE GOLS CLUBE VISITANTE  for um número negativo")
    void cadastrarCenario7_1() throws Exception {
        Integer qtdeGolsClubeVisitante = -1;
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                "São Paulo",
                "Pacaembu",
                1,
                qtdeGolsClubeVisitante,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));


        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"qtdeGolsClubeVisitante","mensagem":"A quantidade de gols do clube visitante não pode ser negativa"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando  RESULTADO  clube mandante  não for VITORIA, DERROTA OU EMPATE")
    void cadastrarCenario8() throws Exception {
        Resultado resultadoClubeMandante = null;
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                "São Paulo",
                "Pacaembu",
                1,
                0,
                resultadoClubeMandante,
                Resultado.DERROTA,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"resultadoClubeMandante","mensagem":"Defina o resultado final do Clube Mandante - VITORIA, DERROTA OU EMPATE"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando  RESULTADO  clube mandante  não for VITORIA, DERROTA OU EMPATE")
    void cadastrarCenario9() throws Exception {
        Resultado resultadoClubeVisitante = null;
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                "São Paulo",
                "Pacaembu",
                1,
                0,
                Resultado.VITORIA,
                resultadoClubeVisitante,
                data
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"resultadoClubeVisitante","mensagem":"Defina o resultado final do Clube Visitante - VITORIA, DERROTA OU EMPATE"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando  a DATA-HORA  não for inserida")
    void cadastrarCenario10() throws Exception {
        LocalDateTime dataHora = null;
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                "São Paulo",
                "Pacaembu",
                1,
                0,
                Resultado.VITORIA,
                Resultado.DERROTA,
                dataHora
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataHora","mensagem":"Infome a data e a hora da partida, formato YYYY-MM-DDTHH:MM"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando  a DATA-HORA  for no futuro")
    void cadastrarCenario10_1() throws Exception {
        var dataHora = LocalDateTime.now().plusMinutes(1);
        var dadosCadastroPartida = new DadosCadastroPartida(
                "Flamengo",
                "São Paulo",
                "Pacaembu",
                1,
                0,
                Resultado.VITORIA,
                Resultado.DERROTA,
                dataHora
        );

        when(partidaService.salvar(dadosCadastroPartida))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataHora","mensagem":"Não é possível salvar uma partida em uma data futura"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME dos clubes forem REPETIDOS")
    void cadastrarCenario11() throws Exception {
        String mesmoNome = "Flamengo";

        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                mesmoNome,
                mesmoNome,
                "Pacaembu",
                5,
                1,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        Mockito.doThrow(new ValidacaoException("Você não pode cadastrar  um único clube para partida"))
                .when(validadorClubeRepetido).validar(dadosCadastroPartida);

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                Você não pode cadastrar  um único clube para partida""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
        verify(validadorClubeRepetido).validar(dadosCadastroPartida);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 409 quando o NOME dos clubes não existir")
    void cadastrarCenario12() throws Exception {
        String nomeClubeMandante = "XXX";
        String nomeClubeVisitante = "Flamengo";
        var data = LocalDate.of(2002, 2, 22).atStartOfDay();
        var dadosCadastroPartida = new DadosCadastroPartida(
                nomeClubeMandante,
                nomeClubeVisitante,
                "Pacaembu",
                5,
                1,
                Resultado.VITORIA,
                Resultado.DERROTA,
                data
        );

        Mockito.doThrow(new ConflitException("Clube mandante não localizado pelo nome: " + nomeClubeMandante))
                .when(validadorClubeRepetido).validar(dadosCadastroPartida);

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        verify(validadorClubeRepetido).validar(dadosCadastroPartida);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 409 quando o NOME dos clubes não existir")
    void cadastrarCenario13() throws Exception {

        Mockito.doThrow(new ConflitException("Clube visitante não localizado pelo nome: "))
                .when(validadorClubesExistentes).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        verify(validadorClubesExistentes).validar(any(DadosCadastroPartida.class));
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando um dos clubes perder e não classificarem como derrota")
    void cadastrarCenario14() throws Exception {

        Mockito.doThrow(new ValidacaoException("teste"))
                .when(validadorDerrotaClube).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(validadorDerrotaClube).validar(any(DadosCadastroPartida.class));
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o local sede do clube mandante for diferente da localizacao do estadio")
    void cadastrarCenario15() throws Exception {

        Mockito.doThrow(new ValidacaoException("teste"))
                .when(validadorEhClubeMandante).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(validadorEhClubeMandante).validar(any(DadosCadastroPartida.class));
    }


    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o resultado da partida  for empate e nao classificarem ambos clubes com empate")
    void cadastrarCenario16() throws Exception {

        Mockito.doThrow(new ValidacaoException("teste"))
                .when(validadorEmpateNaPartida).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(validadorEmpateNaPartida).validar(any(DadosCadastroPartida.class));
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando ja houver alguma partida cadastrada neste estadio com o mesmo horario")
    void cadastrarCenario17() throws Exception {

        Mockito.doThrow(new ValidacaoException("teste"))
                .when(validadorMesmoHorarioEstadio).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(validadorMesmoHorarioEstadio).validar(any(DadosCadastroPartida.class));
    }


    @Test
    @DisplayName("Deveria devolver codigo http 409 quando ja houver alguma partida cadastrada neste estadio com o mesmo horario")
    void cadastrarCenario18() throws Exception {

        Mockito.doThrow(new ConflitException("teste"))
                .when(validadorPartidaAntesDataCriacaoClube).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        verify(validadorPartidaAntesDataCriacaoClube).validar(any(DadosCadastroPartida.class));
    }

    @Test
    @DisplayName("Deveria devolver codigo http 409 quando ja houver alguma partida cadastrada neste estadio com o mesmo horario")
    void cadastrarCenario19() throws Exception {

        Mockito.doThrow(new ConflitException("teste"))
                .when(validadorPartidaAntesDataCriacaoClube).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        verify(validadorPartidaAntesDataCriacaoClube).validar(any(DadosCadastroPartida.class));
    }

    @Test
    @DisplayName("Deveria devolver codigo http 409 quando ja houver alguma partida cadastrada neste estadio num intervalo inferior a 48 horas")
    void cadastrarCenario20() throws Exception {

        Mockito.doThrow(new ConflitException("teste"))
                .when(validadorPartidaHorariosProximos).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        verify(validadorPartidaHorariosProximos).validar(any(DadosCadastroPartida.class));
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o clube mandante vencer e não classificar o resultado como vitoria")
    void cadastrarCenario21() throws Exception {

        Mockito.doThrow(new ValidacaoException("teste"))
                .when(validadorVitoriaClubeMandante).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(validadorVitoriaClubeMandante).validar(any(DadosCadastroPartida.class));
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o clube visitante vencer e não classificar o resultado como vitoria")
    void cadastrarCenario22() throws Exception {

        Mockito.doThrow(new ValidacaoException("teste"))
                .when(validadorVitoriaClubeVisitante).validar(any(DadosCadastroPartida.class));

        var response = mvc.perform(post("/partida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(validadorVitoriaClubeVisitante).validar(any(DadosCadastroPartida.class));
    }

    @Test
    void atualizar() {
    }


    //O Cenário de atualização compartilha da mesma DTO cadastro Partida e sua validações
    @Test
    @DisplayName("Deveria devolver codigo http 400 quando informacoes estao invalidas")
    void atualizarCenario1() throws Exception {
        Long id = 1L;
        var response = mvc.perform(put("/partida/{id}", id))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria devolver codigo http 200 e retornar uma partida atualizada")
    void atualizarCenario2() throws Exception {
        Long id = 1L;
        when(partidaService.atualizarPartidaPeloId(any(DadosCadastroPartida.class), anyLong())).thenReturn(partidaMandanteVitoria);

        var response = mvc.perform(put("/partida/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        var jsonEsperado = dadosDetalhadamentoPartidaJacksonTester.write(
                dadosDetalhadamentoPartida
        ).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 404 quando o id informado for de uma partida inexistente")
    void atualizarCenario3() throws Exception {
        Long id = 1L;
        when(partidaService.atualizarPartidaPeloId(any(DadosCadastroPartida.class), anyLong()))
                .thenThrow(new EntityNotFoundException("Partida não encontrada pelo ID"));

        var response = mvc.perform(put("/partida/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

    }

    @Test
    @DisplayName("Deveria devolver codigo http 204 quando a partida é deletada")
    void deletarCenario1() throws Exception {
        Long id = 1L;

        when(partidaService.validarId(eq(id)))
                .thenReturn(any(Partida.class));

        var response = mvc.perform(delete("/partida/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

    }

    @Test
    @DisplayName("Deveria devolver codigo http 404 quando o id informado for de uma partida inexistente")
    void deletarCenario2() throws Exception {
        Long id = 1L;

        when(partidaService.validarId(eq(id)))
                .thenThrow(new EntityNotFoundException("Partida não encontrada pelo ID"));

        var response = mvc.perform(delete("/partida/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando as nada for informado")
    void deletarCenario3() throws Exception {
        Long id = 1L;

        when(partidaService.validarId(eq(id)))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(delete("/partida/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

    }


    @Test
    @DisplayName("Deveria devolver codigo http 201 e retornar uma partida for  criada")
    void BuscarIdCenario1() throws Exception {

        when(partidaService.validarId(anyLong())).thenReturn(partidaMandanteVitoria);

        var response = mvc.perform(get("/partida/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        var jsonEsperado = dadosDetalhadamentoPartidaJacksonTester.write(
                dadosDetalhadamentoPartida
        ).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 404 quando o id informado for de uma partida inexistente")
    void buscarIdCenario2() throws Exception {
        Long id = 1L;

        when(partidaService.validarId(eq(id)))
                .thenThrow(new EntityNotFoundException("Partida não encontrada pelo ID"));

        var response = mvc.perform(get("/partida/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando informacoes estiverem erradas")
    void buscarIdCenario3() throws Exception {
        Long id = 1L;

        when(partidaService.validarId(eq(id)))
                .thenThrow(new ValidacaoException("teste"));

        var response = mvc.perform(get("/partida/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroPartidaJacksonTester.write(
                                dadosCadastroPartida
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 quando TODOS os parametros NÃO estiverem selecionados e retorna uma lista de partidas")
    void listarPartidasCenario1() throws Exception {
        var listaPartidas = List.of(partidaMandanteVitoria, partidaMandanteDerrota, partidaEmpate);
        Page<Partida> paginaPartida = new PageImpl<>(listaPartidas);

        when(partidaService.filtrarParams(eq(null), eq(null),eq(null), any(Pageable.class)))
                .thenReturn(paginaPartida);

        var response = mvc.perform(get("/partida")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaPartidas.size());

        verify(partidaService).filtrarParams( eq(null), eq(null),eq(null), any(Pageable.class));
    }


    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro Clube for preenchido e retornar 1 partida")
    void listarPartidasCenario2() throws Exception {
        String nome = "Flamengo";
        var listaPartidas = List.of(partidaMandanteVitoria, partidaMandanteDerrota, partidaEmpate);
        Page<Partida> paginaPartida = new PageImpl<>(listaPartidas);

        when(partidaService.encontrarClubeId(anyLong(), any(Pageable.class)))
                .thenReturn(paginaPartida);

        when(partidaService.filtrarParams(eq(nome), eq(null),eq(null), any(Pageable.class)))
                .thenReturn(paginaPartida);

        var response = mvc.perform(get("/partida")
                        .param("clube", nome)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaPartidas.size());
    }


    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro Estadio for preenchido e retornar as partidas deste estadio")
    void listarPartidasCenario3() throws Exception {
        String nome = "Flamengo";
        var listaPartidas = List.of(partidaMandanteVitoria, partidaMandanteDerrota, partidaEmpate);
        Page<Partida> paginaPartida = new PageImpl<>(listaPartidas);

        when(partidaService.filtrarParams(eq(null), eq(nome),eq(null), any(Pageable.class)))
                .thenReturn(paginaPartida);

        var response = mvc.perform(get("/partida")
                        .param("estadio", nome)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaPartidas.size());
    }

    @Test
    @DisplayName("FILTRO AVANCADO - Deveria retornar código http 200 quando o parametro RANKING for preenchido com a expressão - goleadas - retornando as partidas com diferencas de gols de no minimo 3")
    void listarPartidasCenario4() throws Exception {
        String ranking = "goleadas";
        var listaPartidas = List.of(partidaMandanteVitoria, partidaMandanteDerrota, partidaEmpate);
        Page<Partida> paginaPartida = new PageImpl<>(listaPartidas);

        when(partidaService.filtrarParams(eq(null), eq(null),eq(ranking), any(Pageable.class)))
                .thenReturn(paginaPartida);

        var response = mvc.perform(get("/partida")
                        .param("ranking", ranking)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaPartidas.size());
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro RANKING for preenchido com a expressão - total_jogos - retornando os clubes que mais jogaram")
    void listarPartidasCenario5() throws Exception {
        String ranking = "total_jogos";
        Page<ClubeRankingDTO> paginaRanking = new PageImpl<>(anyList());
        when(partidaService.getRanking(ranking, any(Pageable.class)))
                .thenReturn(paginaRanking);

        var response = mvc.perform(get("/partida")
                        .param("ranking", ranking)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro RANKING for preenchido com a expressão - total_vitorias - retornando os clubes que mais venceram")
    void listarPartidasCenario6() throws Exception {
        String ranking = "total_vitorias";
        Page<ClubeRankingDTO> paginaRanking = new PageImpl<>(anyList());
        when(partidaService.getRanking(ranking, any(Pageable.class)))
                .thenReturn(paginaRanking);

        var response = mvc.perform(get("/partida")
                        .param("ranking", ranking)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro RANKING for preenchido com a expressão - total_gols - retornando os clubes que mais fizeram gols")
    void listarPartidasCenario7() throws Exception {
        String ranking = "total_gols";
        Page<ClubeRankingDTO> paginaRanking = new PageImpl<>(anyList());
        when(partidaService.getRanking(ranking, any(Pageable.class)))
                .thenReturn(paginaRanking);

        var response = mvc.perform(get("/partida")
                        .param("ranking", ranking)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro RANKING for preenchido com a expressão - total_gols - retornando os clubes que mais fizeram gols")
    void listarPartidasCenario8() throws Exception {
        String ranking = "total_pontos";
        Page<ClubeRankingDTO> paginaRanking = new PageImpl<>(anyList());
        when(partidaService.getRanking(ranking, any(Pageable.class)))
                .thenReturn(paginaRanking);

        var response = mvc.perform(get("/partida")
                        .param("ranking", ranking)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro RANKING for preenchido incorretamente retornando uma página vazia")
    void listarPartidasCenario9() throws Exception {
        String ranking = "-";
        Page<ClubeRankingDTO> paginaRanking = new PageImpl<>(anyList());
        when(partidaService.getRanking(ranking, any(Pageable.class)))
                .thenReturn(paginaRanking);

        var response = mvc.perform(get("/partida")
                        .param("ranking", ranking)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

    }


    @Test
    @DisplayName("Deveria retornar codigo http 200 quando TODOS os parametros NÃO estiverem selecionados e retorna uma lista de partidas")
    void listarConfrontoClubes_1()throws Exception  {
        Long idClube = 1L;
        Long idClubeAdversario = 2L;
        var listaPartidas = List.of(partidaMandanteVitoria, partidaMandanteDerrota, partidaEmpate);
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("teste", listaPartidas);

        when(partidaService.listarPartidasRetro(idClube,idClubeAdversario))
                .thenReturn(resultado);

        var response = mvc.perform(get("/partida/confronto/{idClube}/{idClubeAdversario}", idClube, idClubeAdversario)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

    }

    @Test
    @DisplayName("Deveria retornar codigo http 404 quando for o idClube ou idClubeAdversario não existirem no banco de dados ")
    void listarConfrontoClubes_2()throws Exception  {
        Long idClube = 0L;
        Long idClubeAdversario = 0L;
        var listaPartidas = List.of(partidaMandanteVitoria, partidaMandanteDerrota, partidaEmpate);
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("teste", listaPartidas);

        when(partidaService.listarPartidasRetro(idClube,idClubeAdversario))
                .thenThrow(new EntityNotFoundException("teste"));

        var response = mvc.perform(get("/partida/confronto/{idClube}/{idClubeAdversario}", idClube, idClubeAdversario)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

    }
}