package com.team.placar.controller;

import com.team.placar.domain.partida.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
        var partida = service.salvar(dados);
        var uri = uriBuilder.path("{partida/{id}").buildAndExpand(partida.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhadamentoPartida(partida));
    }

    @PutMapping("{id}")
    @Transactional
    public ResponseEntity atualizar(@RequestBody @Valid DadosCadastroPartida dados, @PathVariable Long id) {
        var partida = service.atualizarPartidaPeloId(dados,id);
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
    public ResponseEntity<Page<DadosDetalhadamentoPartida>> listarPartidas(
            @RequestParam(required = false) String clube,
            @RequestParam(required = false) String estadio,
            @PageableDefault(size = 10) Pageable paginacao){

        var page = service.filtrarParams(clube, estadio, paginacao);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/confronto/{idClube}/{idClubeAdversario}")
    public ResponseEntity listarClubes(@PathVariable Long idClube, @PathVariable Long idClubeAdversario) {
        var page = service.listarPartidasRetro(idClube, idClubeAdversario);
        return ResponseEntity.ok(page);
    }

}
