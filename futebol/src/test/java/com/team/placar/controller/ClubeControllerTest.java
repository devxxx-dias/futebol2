package com.team.placar.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team.placar.domain.clube.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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

    @Autowired
    JacksonTester<DadosClubeCadastro> dadosCadastroJacksonTester;

    @Autowired
    JacksonTester<DadosClubeAtualizacao> dadosAtualizacaoJacksonTester;

    @Autowired
    JacksonTester<DadosClubeDetalhadamento> dadosDetalhadamentoJacksonTester;

    @Autowired
    JacksonTester<Page<DadosClubeDetalhadamento>> dadosDetalhadamentoJacksonPage;
    @Autowired
    private HttpSession httpSession;

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
                                new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true)
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
    @DisplayName("Deveria devolver codigo http 400 quando o nome não for inserido")
    void cadastrarCenario3() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro(null, "SP", "São Paulo", data, true);


        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"nome","mensagem":"O nome do clube precisa ser inserido"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a sigla do estado não for inserido ")
    void cadastrarCenario4() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "", "São Paulo", data, true);


        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ter exatamente 2 caracteres e apenas letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a sigla do estado fora do padão de 2 letras")
    void cadastrarCenario4_1() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "S", "São Paulo", data, true);


        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ter exatamente 2 caracteres e apenas letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a sigla do estado tiver mais de 2 letras")
    void cadastrarCenario4_2() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SSP", "São Paulo", data, true);


        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"siglaEstado","mensagem":"SiglaEstado deve ter exatamente 2 caracteres e apenas letras"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o localSede não for inserido")
    void cadastrarCenario5() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", "", data, true);


        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"localSede","mensagem":"A localidade da sede deve ser inserida"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando a data inserida for uma data futura")
    void cadastrarCenario6() throws Exception {
        var data = LocalDate.of(2025, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);


        var response = mvc.perform(post("/clube")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosCadastroJacksonTester.write(dadosCadastro).getJson())).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataCriacao","mensagem":"Só é permitido o cadastro de partidas já realizadas"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o status for null e não aceita os valores true ou false")
    void cadastrarCenario7() throws Exception {
        var data = LocalDate.of(2002, 02, 22);
        var dadosCadastro = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, null);


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
    @DisplayName("Deveria devolver codigo http 400 quando o nome não for inserido")
    void atualizarCenario2() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro(null, "SP", "São Paulo", data, true);

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
    @DisplayName("Deveria devolver codigo http 400 quando a sigla do estado não for inserido ")
    void atualizarCenario3() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "", "São Paulo", data, true);


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
    void atualizarCenario4() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "S", "São Paulo", data, true);


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
    @DisplayName("Deveria devolver codigo http 400 quando a sigla do estado tiver mais de 2 letras")
    void atualizarCenario5() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SSP", "São Paulo", data, true);
        ;

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
    @DisplayName("Deveria devolver codigo http 400 quando o localSede não for inserido")
    void atualizarCenario6() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2002, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "", data, true);
        ;

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
    @DisplayName("Deveria devolver codigo http 400 quando a data inserida for uma data futura")
    void atualizarCenario7() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2025, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, true);
        ;

        var response = mvc.perform(put("/clube/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroJacksonTester.write(dadosAtualizados).getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        var expectedErrorMessage = """
                [{"campo":"dataCriacao","mensagem":"Só é permitido o cadastro de partidas já realizadas"}]""";

        assertThat(response.getContentAsString()).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("Deveria devolver codigo http 400 quando o status for null e não aceita os valores true ou false")
    void atualizarCenario8() throws Exception {
        var id = 1L;
        var data = LocalDate.of(2022, 02, 22);
        var dadosAtualizados = new DadosClubeCadastro("Palmeiras", "SP", "São Paulo", data, null);
        ;

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
    @DisplayName("Deveria retornar um clube pelo Id informado")
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

        var jsonEsperado = dadosDetalhadamentoJacksonTester.write(new DadosClubeDetalhadamento(clubleEncontrado)).getJson();

        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
    }

    @Test
    @DisplayName("Deveria retornar codigo http 200 quando todos os parametros nao estiverem selecionados e retorna uma lista de clubes")
    void listarClubes() throws Exception {
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

        // Esse objectMapper é para conseguir ler as datas(localtime e date)
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());
        JsonNode contentNode = jsonResponse.get("content");

        // Ensure the content node is an array and has the expected size
        assertThat(contentNode.isArray()).isTrue();
        assertThat(contentNode.size()).isEqualTo(listaClubes.size());

        // Optional: Verify the exact content if needed
        List<DadosClubeDetalhadamento> clubesFromResponse = objectMapper.convertValue(
                contentNode,
                new TypeReference<List<DadosClubeDetalhadamento>>() {}
        );
        List<DadosClubeDetalhadamento> expectedClubes = listaClubes.stream()
                .map(DadosClubeDetalhadamento::new)
                .collect(Collectors.toList());

        assertThat(clubesFromResponse).isEqualTo(expectedClubes);

        // Verify the service method was called correctly
        verify(clubeService).filtrarParams(eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro nome for preenchido e retornar 1 clube")
    void listarClubes_1() throws Exception {
        var data = LocalDate.of(2002,2, 22);
        var clube = new Clube(new DadosClubeCadastro("Palmeiras", "SP","São Paulo",data, true));
        var listaClubes = List.of(clube);
        Page<Clube> page = new PageImpl<>(listaClubes);

        when(clubeService.filtrarParams(eq("Palmeiras"), eq(null), eq(null), eq(null),  any(Pageable.class)))
                .thenReturn(page);

        var response = mvc.perform(get("/clube")
                .param("nome", "Palmeiras")
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
                new TypeReference<List<DadosClubeDetalhadamento>>() {}
        );
        List<DadosClubeDetalhadamento> expectedClubes = listaClubes.stream()
                .map(DadosClubeDetalhadamento::new)
                .collect(Collectors.toList());

        assertThat(clubesFromResponse).isEqualTo(expectedClubes);
    }



    @Test
    @DisplayName("Deveria retornar código http 200 quando o parametro nome for preenchido com um clube inexistente no banco de dados")
    void listarClubes_2() throws Exception {

        when(clubeService.findByNome(eq("aaa"), any(Pageable.class))).thenReturn(null);

        var response = mvc.perform(get("/clube")
                        .param("nome", "aaa")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentLength()).isEqualTo(0);
        verify(clubeService).filtrarParams(eq("aaa"), eq(null), eq(null), eq(null), any(Pageable.class));

    }

    @Test
    void restropctoGeral() {
    }
}