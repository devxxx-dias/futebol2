package com.team.placar.controller;


import com.team.placar.domain.clube.DadosClubeDetalhadamento;
import com.team.placar.domain.estadio.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@RestController
@RequestMapping("estadio")
public class EstadioController {

    @Autowired
    private EstadioRepository repository;

    @Autowired
    private EstadioService service;


    @PostMapping
    @Transactional
    public ResponseEntity cadastrar(
            @RequestBody @Valid DadosCadastroEstadio dados,
            UriComponentsBuilder uriBuilder){
        var estadio = service.salvar(dados);
        var uri = uriBuilder.path("estadio/{id}").buildAndExpand(estadio.getId()).toUri();
        return  ResponseEntity.created(uri).body(new DadosDetalhadamentoEstadio(estadio));
    }

    @PutMapping("{id}")
    @Transactional
    public ResponseEntity atualizar(
            @RequestBody @Valid DadosCadastroEstadio dados,
            @PathVariable Long id){
        System.out.println(id);
        var estadio = service.atualizar(dados, id);
        return ResponseEntity.ok().body(new DadosDetalhadamentoEstadio(estadio));
    }

    @GetMapping("{id}")
    public ResponseEntity buscar(@PathVariable Long id){
        var estadio = service.buscar(id);
        return ResponseEntity.ok().body(new DadosDetalhadamentoEstadio(estadio));
    }

    @GetMapping
    public ResponseEntity<Page<DadosDetalhadamentoEstadio>> listarEstadios(
            @PageableDefault(size = 10, sort = {"nome"})
            Pageable paginacao){
        var page = repository.findAll(paginacao).map(DadosDetalhadamentoEstadio::new);
        return ResponseEntity.ok(page);
    }



}
