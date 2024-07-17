package com.team.placar.controller;

import com.team.placar.domain.estadio.DadosCadastroEstadio;
import com.team.placar.domain.estadio.Estadio;
import com.team.placar.domain.partida.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team.placar.domain.clube.*;
import com.team.placar.infra.securtiy.tratamentoExceptions.ConflitException;
import com.team.placar.infra.securtiy.tratamentoExceptions.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class PartidaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PartidaService partidaService;

    @MockBean
    private PartidaRepository partidaRepository;

    @Autowired
    JacksonTester<DadosCadastroPartida> dadosCadastroPartidaJacksonTester;

    @Autowired
    JacksonTester<DadosDetalhadamentoPartida> dadosDetalhadamentoPartidaJacksonTester;

    @Autowired
    JacksonTester<Page<DadosDetalhadamentoPartida>> dadosDetalhadamentoJacksonTesterPage;

    private LocalDateTime data = LocalDate.of(2002, 2, 22).atStartOfDay();
    private  DadosCadastroPartida dadosCadastroPartida = new DadosCadastroPartida("Palmeiras",
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
    @DisplayName("Deveria devolver codigo http 400 quando informacoes estao invalidas")
    void cadastrarCenario1() throws Exception {
        var response = mvc.perform(post("/partida"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria devolver codigo http 201 e retornar uma partida for  criada")
    void cadastrarCenario2() throws Exception {

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
    void atualizar() {
    }

    @Test
    void deletar() {
    }

    @Test
    void buscarId() {
    }

    @Test
    void listarPartidas() {
    }
}