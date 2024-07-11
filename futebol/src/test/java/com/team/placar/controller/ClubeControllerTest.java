package com.team.placar.controller;

import com.team.placar.domain.clube.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

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


    @Test
    @DisplayName("Deveria devolver codigo http 400 quando informacoes estao invalidas")
    void cadastrarCenario1() throws Exception {
        var response = mvc.perform(post("/clube"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria devolver codigo http 200 e retornar um clube criado")
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
                new DadosClubeDetalhadamento(null, "Palmeiras", "SP", "São Paulo", data, "Ativo")
        ).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);

    }

    @Test
    void atualizarCenario1() throws Exception {
        Clube clubeInvalido = new Clube();
        clubeInvalido.setNome("Palmeiras");
//        var response = mvc.perform(put("/clube")
//                        .contentType("application/json")
//                        .content(objectMapper.writeValueAsString(clubeInvalido)))
//                .andReturn().getResponse();
//
//        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void deletar() {
    }

    @Test
    void buscar() {
    }

    @Test
    void listarClubes() {
    }

    @Test
    void restropctoGeral() {
    }
}