package com.enoc.transaction.infrastructure.rest;

import com.enoc.transaction.application.service.TransactionService;
import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import java.util.List;
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
    public Mono<ResponseEntity<TransactionResponseDto>> create(@RequestBody Mono<TransactionRequestDTO> request) {
        return request
                .flatMap(transactionService::create)
                .map(response -> ResponseEntity.status(201).body(response));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TransactionResponseDto>> getById(@PathVariable String id) {
        return transactionService.findById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping
    public Flux<TransactionResponseDto> getAll() {
        return transactionService.findAll();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<TransactionResponseDto>> update(
            @PathVariable String id,
            @RequestBody Mono<TransactionRequestDTO> request) {
        return request
                .flatMap(dto -> transactionService.update(id, dto))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return transactionService.deleteTransactionByLogicalState(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/count")
    public Mono<Long> countByAccountIdAndType(@RequestParam String accountId, @RequestParam List<TransactionType> types) {
        return transactionService.countByAccountIdAndTypeIn(accountId, types);
    }


}