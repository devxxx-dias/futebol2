package com.team.placar.controller;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class ClubeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ClubeService clubeService;

    @MockBean
    private ClubeRepository clubeRepository;

    @Autowired
    JacksonTester<DadosClubeCadastro> dadosCadastroJacksonTester;

    @Autowired
    JacksonTester<DadosClubeAtualizacao> dadosAtualizacaoJacksonTester;

    @Autowired
    JacksonTester<DadosClubeDetalhadamento> dadosDetalhadamentoJacksonTester;

    @Autowired
    JacksonTester<Page<DadosClubeDetalhadamento>> dadosDetalhadamentoJacksonPage;

    @Autowired
    JacksonTester<DadosRestropctoClubeDetalhadamento> dadosRestropctoDetalhadamentoJackson;

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando informacoes estao invalidas")
    void cadastrarCenario1() throws Exception {
        var response = mvc.perform(post("/clube"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria devolver codigo http 201 e retornar um clube criado")
    void cadastrarCenario2() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);
        when(clubeService.salvar(any())).thenReturn(new Clube(dadosCadastro));

        var response = mvc.perform(post("/clube")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(
                                dadosCadastro
                        ).getJson())
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        var jsonEsperado = dadosDetalhadamentoJacksonTester.write(
                new DadosClubeDetalhadamento(null, "Palmeiras", "SP", "São Paulo", data, true)
        ).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do clube não for inserido")
    void cadastrarCenario3() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro(null, "SP", "São Paulo", data, true);

        when(clubeService.salvar(any())).thenThrow(new ValidacaoException(""));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nome","mensagem":"O nome do clube precisa ser inserido"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do clube for menor que 2 letras")
    void cadastrarCenario3_1() throws Exception {
        String nome = "P";
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro(nome, "SP", "São Paulo", data, true);

        when(clubeService.salvar(any())).thenThrow(new ValidacaoException(""));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nome","mensagem":"O nome clube dever possuir no mínimo 2 letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }
    @Test
    @DisplayName("Deveria devolver codigo http 409 quando o NOME do clube já existir no mesmo Estado")
    void cadastrarCenario3_2() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("São Paulo", "SP", "São Paulo", data, true);

        when(clubeService.salvar(any())).thenThrow(new ConflitException("Já existe um clube cadastrado com esse nome neste estado"));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

        var expectedErrorMessage = "Já existe um clube cadastrado com esse nome neste estado";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a SIGLAESTADO não for inserido ")
    void cadastrarCenario4() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "", "São Paulo", data, true);

        when(clubeService.salvar(any())).thenReturn(new Clube(dadosCadastro));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ter exatamente 2 caracteres e apenas letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a SIGLAESTADO fora do padrão de 2 letras")
    void cadastrarCenario4_1() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "S", "São Paulo", data, true);

        when(clubeService.salvar(any())).thenReturn(new Clube(dadosCadastro));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ter exatamente 2 caracteres e apenas letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a SIGLAESTADO tiver mais de 2 letras")
    void cadastrarCenario4_2() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SSP", "São Paulo", data, true);

        when(clubeService.salvar(any())).thenReturn(new Clube(dadosCadastro));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ter exatamente 2 caracteres e apenas letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a SIGLAESTADO não pertencer a algum estado brasileiro ")
    void cadastrarCenario4_3() throws Exception {
        String siglaEstadoInexistente = "LL";
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", siglaEstadoInexistente, "São Paulo", data, true);

        when(clubeService.salvar(any())).thenThrow(new ValidacaoException(""));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ser um estado válido do Brasil"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o LOCALSEDE não for inserido")
    void cadastrarCenario5() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", null, data, true);

        when(clubeService.salvar(any()))
                .thenThrow(new ValidacaoException(""));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"localSede","mensagem":"A localidade da sede deve ser inserida"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a DATA inserida for uma data futura")
    void cadastrarCenario6() throws Exception {
        var data = LocalDate.of(2025, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);

        when(clubeService.salvar(any()))
                .thenThrow(new ValidacaoException(""));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataCriacao","mensagem":"Só é permitido inserir uma data no passado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a DATA não for no passado")
    void cadastrarCenario6_1() throws Exception {
        LocalDate data = LocalDate.now().plusYears(1);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);

        when(clubeService.salvar(any()))
                .thenThrow(new ValidacaoException(""));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataCriacao","mensagem":"Só é permitido inserir uma data no passado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a DATA não for preenchida")
    void cadastrarCenario6_2() throws Exception {

        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", null, true);

        when(clubeService.salvar(any()))
                .thenThrow(new ValidacaoException(""));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataCriacao","mensagem":"O campo dataCriacao deve ser preenchido"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o status for null e não aceita os valores true ou false")
    void cadastrarCenario7() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, null);

        when(clubeService.salvar(any())).thenReturn(new Clube(dadosCadastro));
        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"status","mensagem":"Você deve apenas inserir os valores true ou false"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo 200 quando o clube for atualizado")
    void atualizarCenario1() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 2, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);
        var clubeAtualizado = new Clube(dadosAtualizados);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenReturn(clubeAtualizado);

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        var jsonEsperado = dadosDetalhadamentoJacksonTester.write(
                new DadosClubeDetalhadamento(clubeAtualizado)
        ).getJson();

        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME não for inserido")
    void atualizarCenario2() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro(null, "SP", "São Paulo", data, true);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenReturn(null);

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nome","mensagem":"O nome do clube precisa ser inserido"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o NOME do clube for menor que 2 letras")
    void atualizarCenario2_1() throws Exception {
        Long id = 1L;
        String nome = "P";
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro(nome, "SP", "São Paulo", data, true);

        when(clubeService.atualizar(eq(id),any())).thenThrow(new ValidacaoException(""));
        var response = mvc.perform(put("/clube/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nome","mensagem":"O nome clube dever possuir no mínimo 2 letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 409 quando o NOME do clube já existir no mesmo Estado e de outro ID")
    void atualizarCenario2_2() throws Exception {
        Long id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("São Paulo", "SP", "São Paulo", data, true);

        when(clubeService.atualizar(eq(id),any())).thenThrow(new ConflitException("Já existe um clube cadastrado com esse nome neste estado"));
        var response = mvc.perform(put("/clube/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

        var expectedErrorMessage = "Já existe um clube cadastrado com esse nome neste estado";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a SIGLAESTADO não for inserido ")
    void atualizarCenario3() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "", "São Paulo", data, true);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenReturn(null);

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ter exatamente 2 caracteres e apenas letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a sigla do estado fora do padão de 2 letras")
    void atualizarCenario3_1() throws Exception {
        String siglaEstado = "S";
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", siglaEstado, "São Paulo", data, true);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenReturn(null);

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ter exatamente 2 caracteres e apenas letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a SIGLAESTADO tiver mais de 2 letras")
    void atualizarCenario3_2() throws Exception {
       String siglaEstado = "SSP";
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", siglaEstado, "São Paulo", data, true);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenReturn(null);

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ter exatamente 2 caracteres e apenas letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a SIGLAESTADO não pertencer a algum estado brasileiro ")
    void atualizarCenario3_3() throws Exception {
        String siglaEstadoInexistente = "LL";
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", siglaEstadoInexistente, "São Paulo", data, true);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ser um estado válido do Brasil"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o LOCALSEDE não for inserido")
    void atualizarCenario4() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "", data, true);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"localSede","mensagem":"A localidade da sede deve ser inserida"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a DATA inserida for uma data futura")
    void atualizarCenario5() throws Exception {
        var id = 1L;
        LocalDate data = LocalDate.now().plusYears(1);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataCriacao","mensagem":"Só é permitido inserir uma data no passado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a DATA não for no passado")
    void atualizarCenario5_1() throws Exception {
        var id = 1L;
        LocalDate data = LocalDate.now().plusYears(1);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataCriacao","mensagem":"Só é permitido inserir uma data no passado"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a DATA não for preenchida")
    void atualizarCenario5_2() throws Exception {
        var id = 1L;
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", null, true);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenThrow(new ValidacaoException(""));

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataCriacao","mensagem":"O campo dataCriacao deve ser preenchido"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 409 quando a DATA de criacao fornecida for maior do que a ultima data da partida do clube")
    void atualizarCenario5_3() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 2, 22).plusYears(1);
        LocalDateTime dataCriacaoInicioDoDia = LocalDateTime.now().minusMinutes(5L);

        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);
        var clube = new Clube(dadosAtualizados);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenThrow(new ConflitException("Não é possível cadastrar uma data depois da data de uma partida do clube"));

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

        var expectedErrorMessage = """
                Não é possível cadastrar uma data depois da data de uma partida do clube""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }


    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o status for null e não aceita os valores true ou false")
    void atualizarCenario6() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2022, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, null);

        when(clubeService.atualizar(any(Long.class), any(DadosClubeCadastro.class)))
                .thenReturn(null);

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"status","mensagem":"Você deve apenas inserir os valores true ou false"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }



    @Test
    @DisplayName("Deveria retornar codigo http 204 na exclusão lógica do clube - status false")
    void deletarCenario1() throws Exception {
        Long id = 1L;
        doNothing().when(clubeService).deletar(id);

        var response = mvc.perform(delete("/clube/{id}", id))
                .andReturn().getResponse();

        verify(clubeService).deletar(id);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("Deveria retornar codigo http 404  quando o id não for encontrado")
    void deletarCenario2() throws Exception {
        Long id = 1L;

        //For void Return
        Mockito.doThrow(new EntityNotFoundException())
                .when(clubeService).deletar(id);

        var response = mvc.perform(delete("/clube/{id}", id))
                .andReturn().getResponse();

        verify(clubeService).deletar(id);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 quando encontrado um clube pelo Id informado")
    void buscarCenario1() throws Exception {
        Long id = 1L;
        var data = LocalDate.of(2002, 2, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);
        var clubleEncontrado = new Clube(dadosCadastro);

        when(clubeService.buscar(id))
                .thenReturn(clubleEncontrado);

        var response = mvc.perform(get("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosCadastro)
                                .getJson()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        var jsonEsperado = dadosDetalhadamentoJacksonTester
                .write(new DadosClubeDetalhadamento(clubleEncontrado)).getJson();

        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
    }



    @Test
    @DisplayName("Deveria retornar codigo HTTP 404 quando o clube não for encontrado pelo Id")
    void buscarCenario2() throws Exception {
        Long id = 999L;

        when(clubeService.buscar(id))
                .thenThrow(new EntityNotFoundException("Clube não encontrado pelo id ou não está ativo"));

        var response = mvc.perform(get("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 quando TODOS os parametros NÃO estiverem selecionados e retorna uma lista de clubes")
    void listarClubesCenario1() throws Exception {
        var data = LocalDate.of(2002, 2, 22);
        var clube1 = new Clube(new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true));
        var clube2 = new Clube(new DadosClubeCadastro("Santos", "SP", "Santos", data, true));
        var listaClubes = List.of(clube1, clube2);
        Page<Clube> paginaClubes = new PageImpl<>(listaClubes);

        when(clubeService.filtrarParams(eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(paginaClubes);

        var response = mvc.perform(get("/clube")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaClubes.size());

        List<DadosClubeDetalhadamento> clubesFromResponse = objectMapper.convertValue(
                contentNode,
                new TypeReference<List<DadosClubeDetalhadamento>>() {
                }
        );
        List<DadosClubeDetalhadamento> expectedClubes = listaClubes.stream()
                .map(DadosClubeDetalhadamento::new)
                .collect(Collectors.toList());

        assertThat(clubesFromResponse).isEqualTo(expectedClubes);

        verify(clubeService).filtrarParams(eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro NOME for preenchido e retornar 1 clube")
    void listarClubesCenario2() throws Exception {
        String nome = "Palmeiras";
        var data = LocalDate.of(2002, 2, 22);
        var clube = new Clube(new DadosClubeCadastro(nome, "SP", "São Paulo", data, true));
        var listaClubes = List.of(clube);
        Page<Clube> page = new PageImpl<>(listaClubes);

        when(clubeService.filtrarParams(eq(nome), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        var response = mvc.perform(get("/clube")
                        .param("nome", nome)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaClubes.size());

        List<DadosClubeDetalhadamento> clubesFromResponse = objectMapper.convertValue(
                contentNode,
                new TypeReference<List<DadosClubeDetalhadamento>>() {
                }
        );
        List<DadosClubeDetalhadamento> expectedClubes = listaClubes.stream()
                .map(DadosClubeDetalhadamento::new)
                .collect(Collectors.toList());

        assertThat(clubesFromResponse).isEqualTo(expectedClubes);
    }

    @Test
    @DisplayName("Deveria retornar código http 200 e uma Page vazia quando o NOME for inexistente")
    void listarClubesCenario2_1() throws Exception {
    String nomeInexistente = "aaa";
        when(clubeService.findByNome(eq(nomeInexistente), any(Pageable.class))).thenReturn(null);

        var response = mvc.perform(get("/clube")
                        .param("nome", nomeInexistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(clubeService).filtrarParams(eq(nomeInexistente), eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro SIGLAESTADO for preenchido retornando uma lista de clubes deste estado")
    void listarClubesCenario3() throws Exception {
        var data = LocalDate.of(2002, 2, 22);
        var clube1 = new Clube(new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true));
        var clube2 = new Clube(new DadosClubeCadastro("Santos", "SP", "Santos", data, true));
        var listaClubes = List.of(clube1, clube2);
        Page<Clube> paginaClubes = new PageImpl<>(listaClubes);

        when(clubeService.filtrarParams(eq(null), eq("SP"), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(paginaClubes);

        var response = mvc.perform(get("/clube")
                        .param("siglaEstado", "SP")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaClubes.size());

        List<DadosClubeDetalhadamento> clubesFromResponse = objectMapper.convertValue(
                contentNode,
                new TypeReference<List<DadosClubeDetalhadamento>>() {
                }
        );
        List<DadosClubeDetalhadamento> expectedClubes = listaClubes.stream()
                .map(DadosClubeDetalhadamento::new)
                .collect(Collectors.toList());

        assertThat(clubesFromResponse).isEqualTo(expectedClubes);

    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 quando o campo SIGLAESTADO for inexistente retornando uma page vazia")
    void listarClubesCenario3_1() throws Exception {
        String siglaEstadoErrado = "XX";
        Page<DadosClubeDetalhadamento> paginaVazia = Page.empty();

        when(clubeService.filtrarParams(eq(null), eq(siglaEstadoErrado), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(paginaVazia);

        var response = mvc.perform(get("/clube")
                        .param("siglaEstado", siglaEstadoErrado)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(0);

        verify(clubeService).filtrarParams(eq(null), eq(siglaEstadoErrado), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro SIGLAESTADO for preenchido utilizando o metodo findBySiglaEstado")
    void listarClubesCenario3_2() throws Exception {
        String siglaEstado = "XX";
        when(clubeService.findBySiglaEstado(eq(siglaEstado), any(Pageable.class))).thenReturn(null);

        var response = mvc.perform(get("/clube")
                        .param("siglaEstado", siglaEstado)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(clubeService).filtrarParams(eq(null), eq(siglaEstado), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro LOCALSEDE for preenchido corretamente retornando uma lista de clubes deste estado")
    void listarClubesCenario4() throws Exception {
        String localSede = "São Paulo";
        var data = LocalDate.of(2002, 2, 22);
        var clube1 = new Clube(new DadosClubeCadastro("Palmeiras", "SP", localSede, data, true));
        var clube2 = new Clube(new DadosClubeCadastro("São Paulo", "SP", localSede, data, true));
        var listaClubes = List.of(clube1, clube2);
        Page<Clube> paginaClubes = new PageImpl<>(listaClubes);

        when(clubeService.filtrarParams(eq(null), eq(null), eq(localSede), eq(null), any(Pageable.class)))
                .thenReturn(paginaClubes);

        var response = mvc.perform(get("/clube")
                        .param("localSede", localSede)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaClubes.size());

        List<DadosClubeDetalhadamento> clubesFromResponse = objectMapper.convertValue(
                contentNode,
                new TypeReference<List<DadosClubeDetalhadamento>>() {
                }
        );
        List<DadosClubeDetalhadamento> expectedClubes = listaClubes.stream()
                .map(DadosClubeDetalhadamento::new)
                .collect(Collectors.toList());

        assertThat(clubesFromResponse).isEqualTo(expectedClubes);

    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 quando o campo LOCALSEDE for inexistente retornando uma page vazia")
    void listarClubesCenario4_1() throws Exception {
        String localSedeErrado = "AAA";
        Page<DadosClubeDetalhadamento> paginaVazia = Page.empty();

        when(clubeService.filtrarParams(eq(null), eq(null), eq(localSedeErrado), eq(null), any(Pageable.class)))
                .thenReturn(paginaVazia);

        var response = mvc.perform(get("/clube")
                        .param("localSede", localSedeErrado)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(0);

        verify(clubeService).filtrarParams(eq(null), eq(null), eq(localSedeErrado), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro LOCALSEDE for inserido errado utilizando o metodo findByLocalSede")
    void listarClubesCenario4_2() throws Exception {
        String localSede = "São Paulo";

        when(clubeService.findByLocalSede(eq(localSede), any(Pageable.class))).thenReturn(null);

        var response = mvc.perform(get("/clube")
                        .param("localSede", localSede)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(clubeService).filtrarParams(eq(null), eq(null), eq(localSede), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro STATUS for preenchido com FALSE retornando uma lista de clubes pelo status")
    void listarClubesCenario5() throws Exception {
        String  status = "false";
        var data = LocalDate.of(2002, 2, 22);
        var clube1 = new Clube(new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true));
        var clube2 = new Clube(new DadosClubeCadastro("São Paulo", "SP", "São Paulo", data, true));
        var listaClubes = List.of(clube1, clube2);
        Page<Clube> paginaClubes = new PageImpl<>(listaClubes);

        when(clubeService.filtrarParams(eq(null), eq(null), eq(null), eq(status), any(Pageable.class)))
                .thenReturn(paginaClubes);

        var response = mvc.perform(get("/clube")
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaClubes.size());

        List<DadosClubeDetalhadamento> clubesFromResponse = objectMapper.convertValue(
                contentNode,
                new TypeReference<List<DadosClubeDetalhadamento>>() {
                }
        );
        List<DadosClubeDetalhadamento> expectedClubes = listaClubes.stream()
                .map(DadosClubeDetalhadamento::new)
                .collect(Collectors.toList());

        assertThat(clubesFromResponse).isEqualTo(expectedClubes);

    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro STATUS for preenchido com TRUE retornando uma lista de clubes pelo status")
    void listarClubesCenario5_1() throws Exception {
        String status = "true";
        var data = LocalDate.of(2002, 2, 22);
        var clube1 = new Clube(new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true));
        var clube2 = new Clube(new DadosClubeCadastro("São Paulo", "SP", "São Paulo", data, true));
        var listaClubes = List.of(clube1, clube2);
        Page<Clube> paginaClubes = new PageImpl<>(listaClubes);

        when(clubeService.filtrarParams(eq(null), eq(null), eq(null), eq(status), any(Pageable.class)))
                .thenReturn(paginaClubes);

        var response = mvc.perform(get("/clube")
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaClubes.size());

        List<DadosClubeDetalhadamento> clubesFromResponse = objectMapper.convertValue(
                contentNode,
                new TypeReference<List<DadosClubeDetalhadamento>>() {
                }
        );
        List<DadosClubeDetalhadamento> expectedClubes = listaClubes.stream()
                .map(DadosClubeDetalhadamento::new)
                .collect(Collectors.toList());

        assertThat(clubesFromResponse).isEqualTo(expectedClubes);
    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 quando o campo STATUS for inexistente retornando uma page VAZIA")
    void listarClubesCenario5_2() throws Exception {
        String statusInexistente = "XXXX";
        Page<DadosClubeDetalhadamento> paginaVazia = Page.empty();

        when(clubeService.filtrarParams(eq(null), eq(null), eq(null), eq(statusInexistente), any(Pageable.class)))
                .thenReturn(paginaVazia);

        var response = mvc.perform(get("/clube")
                        .param("status", statusInexistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(0);

        verify(clubeService).filtrarParams(eq(null), eq(null), eq(null), eq("XXXX"), any(Pageable.class));
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro STATUS for inserido ERRADO utilizando o metodo findByStatus")
    void listarClubesCenario5_3() throws Exception {
        String statusInexistente = "XXXX";
        when(clubeService.findByStatus(eq(statusInexistente), any(Pageable.class))).thenReturn(null);

        var response = mvc.perform(get("/clube")
                        .param("status", statusInexistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(clubeService).filtrarParams(eq(null), eq(null), eq(null), eq(statusInexistente), any(Pageable.class));
    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 quando o id do clube for localizado")
    void restropctoGeralCenario1() throws Exception {
        Long id = 1L;
        LocalDate data = LocalDate.of(2002, 2, 22);
        Clube clube = new Clube(new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true));

        when(clubeService.validarId(eq(id))).thenReturn(clube);

        var response = mvc.perform(get("/clube/geral/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    @DisplayName("Deveria retornar codigo http 404 quando o id do clube for localizado")
    void restropctoGeralCenario1_1() throws Exception {
        Long id = 0L;
        when(clubeService.validarId(id)).thenThrow(new EntityNotFoundException()); // Mocking service to return null

        MvcResult result = mvc.perform(get("/clube/geral/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        verify(clubeService).validarId(id);
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 quando passado o id de um clube para gerar a sua retrospectiva")
    void restropctoGeralCenario1_2() throws Exception {
        Long id = 1L;
        LocalDate data = LocalDate.of(2002, 2, 22);
        Clube clube = new Clube(new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true));
        var totalVitorias = 0;
        var totalDerrotas = 0;
        var totalEmpates = 0;
        var totalGolsFeito = 0;
        var totalGolsSofridos = 0;
        DadosRestropctoClubeDetalhadamento dados = new DadosRestropctoClubeDetalhadamento(clube.getNome(), totalVitorias, totalDerrotas, totalEmpates,
                totalGolsFeito, totalGolsSofridos);

        when(clubeService.efeituarRestropctiva(id)).thenReturn(dados);

        var response = mvc.perform(get("/clube/geral/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosRestropctoDetalhadamentoJackson.write(
                        dados
                ).getJson())
        ).andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        var jsonEsperado = dadosRestropctoDetalhadamentoJackson.write(
                dados
        ).getJson();

        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
    }

}