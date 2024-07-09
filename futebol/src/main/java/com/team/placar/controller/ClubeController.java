package com.team.placar.controller;

import com.team.placar.domain.clube.*;
import com.team.placar.domain.partida.DadosDetalhadamentoPartida;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/clube")
public class ClubeController {
    @Autowired
    private ClubeService clubeService;

    @Autowired
    private ClubeRepository clubeRepository;

    @PostMapping
    @Transactional
    public ResponseEntity cadastrar(@RequestBody @Valid DadosClubeCadastro dados, UriComponentsBuilder uriBuilder ) {
        var clube = clubeService.salvar(dados);
        var uri = uriBuilder.path("clube/{id}").buildAndExpand(clube.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosClubeDetalhadamento(clube));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity atualizar(@RequestBody @Valid DadosClubeCadastro dados, @PathVariable Long id) {
        var clube = clubeService.validar(id, dados);
        return ResponseEntity.ok().body(new DadosClubeDetalhadamento(clube));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deletar(@PathVariable Long id) {
        clubeService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}")
    public ResponseEntity buscar(@PathVariable Long id) {
        var clube = clubeService.buscar(id);
        return ResponseEntity.ok().body(new DadosClubeDetalhadamento(clube));
    }

    @GetMapping
    public ResponseEntity<Page<DadosClubeDetalhadamento>> listarClubes(@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {
        var page = clubeRepository.findAll(paginacao).map(DadosClubeDetalhadamento::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/geral/{id}")
    public ResponseEntity restropctoGeral(@PathVariable Long id){
        clubeService.validarId(id);
        var retrospectiva = clubeService.efeituarRestropctiva(id);

        return ResponseEntity.ok(retrospectiva);
    }
//    @GetMapping("/geral/adversarios")
//    public ResponseEntity<Page<DadosDetalhadamentoPartida>> listarClubes(@RequestParam("idClube1") Long idClube1, @RequestParam("idClube1") Long idClube1, Pageable paginacao) {
//        clubeService.validarId();
//        var page = clubeService.listarRestrospectivaId(idClube1, idClube2, paginacao).map(DadosDetalhadamentoPartida::new);
//        return ResponseEntity.ok(page);
//    }

//deverei copiar a query anterior e colocar o id mandante
// e o id do adversario, pegar a resposta e tratar

}
