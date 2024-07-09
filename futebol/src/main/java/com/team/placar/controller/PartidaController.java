package com.team.placar.controller;

import com.team.placar.domain.partida.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("partida")
public class PartidaController {
    @Autowired
    private PartidaRepository repository;

    @Autowired
    private PartidaService service;

    @PostMapping
    @Transactional
    public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroPartida dados, UriComponentsBuilder uriBuilder) {
        var partida = service.validarDados(dados);
        var uri = uriBuilder.path("{partida/{id}").buildAndExpand(partida.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhadamentoPartida(partida));
    }

    @PutMapping("{id}")
    @Transactional
    public ResponseEntity atualizar(@RequestBody @Valid DadosAtualizarPartida dados, @PathVariable Long id) {
        var partida = service.validarDadosID(dados,id);
        return ResponseEntity.ok().body(new DadosDetalhadamentoPartida(partida));
    }

    @DeleteMapping("{id}")
    @Transactional
    public ResponseEntity deletar(@PathVariable Long id){
        var partida = service.validarId(id);
        repository.delete(partida);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}")
    public ResponseEntity buscarId(@PathVariable Long id){
        var partida = service.validarId(id);
        return ResponseEntity.ok().body(new DadosDetalhadamentoPartida(partida));

    }

    @GetMapping
    public ResponseEntity<Page<DadosDetalhadamentoPartida>> listarPartidas(@PageableDefault(size = 10) Pageable paginacao){
        var page = repository.findAll(paginacao).map(DadosDetalhadamentoPartida::new);
        return ResponseEntity.ok(page);
    }

}
