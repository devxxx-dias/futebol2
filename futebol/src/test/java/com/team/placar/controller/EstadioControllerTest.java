package com.team.placar.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team.placar.domain.clube.Clube;
import com.team.placar.domain.clube.DadosClubeCadastro;
import com.team.placar.domain.clube.DadosClubeDetalhadamento;
import com.team.placar.domain.estadio.*;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class EstadioControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EstadioService estadioService;

    @MockBean
    private EstadioRepository estadioRepository;

    @Autowired
    JacksonTester<DadosCadastroEstadio> dadosCadastroEstadioJacksonTester;

    @Autowired
    JacksonTester<DadosDetalhadamentoEstadio>  dadosDetalhadamentoEstadioJacksonTester;

    @Autowired
    JacksonTester<Page<DadosDetalhadamentoEstadio>> dadosPageDetalhadamentoEstadioJacksonPage;

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando informacoes estao invalidas")
    void cadastrarCenario1() throws Exception {
        var response = mvc.perform(post("/estadio"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria devolver codigo http 201 e retornar um estadio for criado")
    void cadastrarCenario2() throws Exception {
        var dadosCadastro = new DadosCadastroEstadio("Pacaembu", "São Paulo", "SP");
        var estadio = new Estadio(dadosCadastro);

        when(estadioService.salvar(dadosCadastro)).thenReturn(estadio);

        var response = mvc.perform(post("/estadio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        var jsonEsperado = dadosDetalhadamentoEstadioJacksonTester.write(
                new DadosDetalhadamentoEstadio("Pacaembu", "São Paulo", "SP")
        ).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do estadio for menor que 3 letras")
    void cadastrarCenario3() throws Exception {
        String nome = "P";
        var dadosCadastro = new DadosCadastroEstadio(nome, "São Paulo", "SP");
        var estadio = new Estadio(dadosCadastro);

        when(estadioService.salvar(dadosCadastro)).thenThrow(new ValidacaoException("teste"));

        var response = mvc.perform(post("/estadio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());


        var expectedErrorMessage = """
                [{"campo":"nome","mensagem":"O Nome do clube não pode ser menor que 3 letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do estadio nao for preenchido")
    void cadastrarCenario3_1() throws Exception {
        String nome = null;
        var dadosCadastro = new DadosCadastroEstadio(nome, "São Paulo", "SP");
        var estadio = new Estadio(dadosCadastro);

        when(estadioService.salvar(dadosCadastro)).thenThrow(new ValidacaoException("teste"));

        var response = mvc.perform(post("/estadio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());


        var expectedErrorMessage = """
                [{"campo":"nome","mensagem":"O nome do estadio precisa ser infomado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 409  quando o NOME do estadio inserido ja estiver cadastrado ")
    void cadastrarCenario3_2() throws Exception {
        String nome = "Palmeiras";
        var dadosCadastro = new DadosCadastroEstadio(nome, "São Paulo", "SP");
        var estadio = new Estadio(dadosCadastro);

        when(estadioService.salvar(dadosCadastro)).thenThrow(new ConflitException("Já existe um estadio cadastrado com este nome"));

        var response = mvc.perform(post("/estadio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());


    }

    @Test
        @DisplayName("Deveria devolver codigo http 400 quando a CIDADE possuir algum numero")
    void cadastrarCenario4() throws Exception {
        String cidade = "amarelo1";
        var dadosCadastro = new DadosCadastroEstadio("Pacaembu", cidade, "SP");
        var estadio = new Estadio(dadosCadastro);

        when(estadioService.salvar(dadosCadastro)).thenThrow(new ValidacaoException("teste"));

        var response = mvc.perform(post("/estadio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"cidade","mensagem":"Verifique se há números em algum nome"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }


    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a SIGLAESTADO não pertencer a algum estado brasileiro ")
    void cadastrarCenario5() throws Exception {
        String siglaEstado = "AA";
        var dadosCadastro = new DadosCadastroEstadio("Palmeiras", "São Paulo", siglaEstado);
        var estadio = new Estadio(dadosCadastro);

        when(estadioService.salvar(dadosCadastro)).thenThrow(new ValidacaoException("teste"));

        var response = mvc.perform(post("/estadio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ser um estado válido do Brasil"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }


    @Test
    @DisplayName("Deveria devolver codigo http 400 quando informacoes estao invalidas")
    void atualizarCenario1() throws Exception {
        Long id = 1L;
        var response = mvc.perform(put("/estadio/{id}",id))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria devolver codigo http 200 e retornar um estadio atualizado")
    void atualizarCenario2() throws Exception {
        var id = 1L;
        var dadosAtualizados = new DadosCadastroEstadio("Pacaembu", "São Paulo", "SP");
        var estadio = new Estadio(dadosAtualizados);

        when(estadioService.atualizar(any(DadosCadastroEstadio.class), anyLong()))
                .thenReturn(estadio);

        var response = mvc.perform(put("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosAtualizados
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        var jsonEsperado = dadosDetalhadamentoEstadioJacksonTester.write(
                new DadosDetalhadamentoEstadio("Pacaembu", "São Paulo", "SP")
        ).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
    }



    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do estadio for menor que 3 letras")
    void atualizarCenario3() throws Exception {
        var id = 1L;
        String nome = "P";
        var dadosCadastro = new DadosCadastroEstadio(nome, "São Paulo", "SP");

        when(estadioService.atualizar(any(DadosCadastroEstadio.class), anyLong())).thenThrow(new ValidacaoException("teste"));

        var response = mvc.perform(put("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());


        var expectedErrorMessage = """
                [{"campo":"nome","mensagem":"O Nome do clube não pode ser menor que 3 letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do estadio nao for preenchido")
    void atualizarCenario3_1() throws Exception {
        var id = 1L;
        String nome = null;
        var dadosCadastro = new DadosCadastroEstadio(nome, "São Paulo", "SP");

        when(estadioService.atualizar(any(DadosCadastroEstadio.class), anyLong())).thenThrow(new ValidacaoException("teste"));

        var response = mvc.perform(put("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());


        var expectedErrorMessage = """
                [{"campo":"nome","mensagem":"O nome do estadio precisa ser infomado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 409  quando o NOME do estadio inserido ja estiver cadastrado ")
    void atualizarCenario3_2() throws Exception {
        var id = 1L;
        String nome = "Palmeiras";
        var dadosCadastro = new DadosCadastroEstadio(nome, "São Paulo", "SP");

        when(estadioService.atualizar(any(DadosCadastroEstadio.class), anyLong())).thenThrow(new ConflitException("Já existe um estadio cadastrado com este nome"));

        var response = mvc.perform(put("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a CIDADE possuir algum numero")
    void atualizarCenario4() throws Exception {
        var id = 1L;
        String cidade = "amarelo1";
        var dadosCadastro = new DadosCadastroEstadio("Pacaembu", cidade, "SP");

        when(estadioService.atualizar(any(DadosCadastroEstadio.class), anyLong())).thenThrow(new ValidacaoException("teste"));

        var response = mvc.perform(put("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"cidade","mensagem":"Verifique se há números em algum nome"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }


    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a SIGLAESTADO não pertencer a algum estado brasileiro ")
    void atualizarCenario5() throws Exception {
        var id = 1L;
        String siglaEstado = "AA";
        var dadosCadastro = new DadosCadastroEstadio("Palmeiras", "São Paulo", siglaEstado);

        when(estadioService.atualizar(any(DadosCadastroEstadio.class), anyLong())).thenThrow(new ValidacaoException("teste"));

        var response = mvc.perform(put("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ser um estado válido do Brasil"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 404 quando não for localizado o id do estadio para ser atualizado")
    void atualizarCenario6() throws Exception {
        var id = 1L;
        var dadosCadastro = new DadosCadastroEstadio("Palmeiras", "São Paulo", "SP");

        when(estadioService.atualizar(any(DadosCadastroEstadio.class), anyLong())).thenThrow(new EntityNotFoundException("Estádio não localizado pelo id"));

        var response = mvc.perform(put("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        var expectedErrorMessage = """
                Estádio não localizado pelo id""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 409 quando já houver algum nome de estadio cadastrado")
    void atualizarCenario7() throws Exception {
        var id = 1L;
        var dadosCadastro = new DadosCadastroEstadio("Palmeiras", "São Paulo", "SP");

        when(estadioService.atualizar(any(DadosCadastroEstadio.class), anyLong())).thenThrow(new ConflitException("Já existe um estadio cadastrado com esse nome"));

        var response = mvc.perform(put("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

        var expectedErrorMessage = """
                Já existe um estadio cadastrado com esse nome""";


        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 200 e retornar um estadio ")
    void buscaEstadioCenario1() throws Exception {
        var id = 1L;
        var dadosAtualizados = new DadosCadastroEstadio("Pacaembu", "São Paulo", "SP");
        var estadio = new Estadio(dadosAtualizados);

        when(estadioService.buscarId(anyLong()))
                .thenReturn(estadio);

        var response = mvc.perform(get("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosAtualizados
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        var jsonEsperado = dadosDetalhadamentoEstadioJacksonTester.write(
                new DadosDetalhadamentoEstadio("Pacaembu", "São Paulo", "SP")
        ).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
    }


    @Test
    @DisplayName("Deveria devolver codigo http 404 quando não for localizado o id do estadio para ser atualizado")
    void buscarEstadioCenario2() throws Exception {
        var id = 1L;
        var dadosCadastro = new DadosCadastroEstadio("Palmeiras", "São Paulo", "SP");

        when(estadioService.buscarId(anyLong())).thenThrow(new EntityNotFoundException("Estádio não localizado pelo id"));

        var response = mvc.perform(get("/estadio/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroEstadioJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        var expectedErrorMessage = """
                Estádio não localizado pelo id""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    void listarEstadios() {
    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 com uma lista de estadios paginados quando o endpoint get /estadio for chamado")
    void listarEstadioCenario1() throws Exception {
        var dadosAtualizados1 = new DadosCadastroEstadio("Pacaembu", "São Paulo", "SP");
        var estadio1 = new Estadio(dadosAtualizados1);
        var dadosAtualizados2 = new DadosCadastroEstadio("São Paulo", "São Paulo", "SP");
        var estadio2 = new Estadio(dadosAtualizados2);
        var listaEstadios = List.of(estadio1, estadio2);
        Page<Estadio> paginaEstadio = new PageImpl<>(listaEstadios);

        when(estadioService.listarEstadios(any(Pageable.class)))
                .thenReturn(paginaEstadio.map(DadosDetalhadamentoEstadio::new));

        var response = mvc.perform(get("/estadio")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaEstadios.size());

        List<DadosDetalhadamentoEstadio> estadiosFromResponse = objectMapper.convertValue(
                contentNode,
                new TypeReference<List<DadosDetalhadamentoEstadio>>() {
                }
        );
        List<DadosDetalhadamentoEstadio> expectedEstadio = listaEstadios.stream()
                .map(DadosDetalhadamentoEstadio::new)
                .collect(Collectors.toList());

        assertThat(estadiosFromResponse).isEqualTo(expectedEstadio);

        verify(estadioService).listarEstadios(any(Pageable.class));
    }

}