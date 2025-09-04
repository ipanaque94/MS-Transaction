package com.enoc.transaction.controller;

import com.enoc.transaction.service.TransactionService;
import org.openapitools.model.TransactionRequest;
import org.openapitools.model.TransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public Mono<ResponseEntity<TransactionResponse>> create(@RequestBody Mono<TransactionRequest> request) {
        return request
                .flatMap(transactionService::create)
                .map(response -> ResponseEntity.status(201).body(response));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TransactionResponse>> getById(@PathVariable String id) {
        return transactionService.findById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping
    public Flux<TransactionResponse> getAll() {
        return transactionService.findAll();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<TransactionResponse>> update(
            @PathVariable String id,
            @RequestBody Mono<TransactionRequest> request) {
        return request
                .flatMap(req -> transactionService.update(id, req))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return transactionService.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}