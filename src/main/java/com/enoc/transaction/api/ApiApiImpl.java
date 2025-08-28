package com.enoc.transaction.api;

import com.enoc.transaction.service.TransactionService;
import org.openapitools.api.ApiApi;
import org.openapitools.model.TransactionRequest;
import org.openapitools.model.TransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RestController
public class ApiApiImpl implements ApiApi {

    private final TransactionService transactionService;

    public ApiApiImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> createTransaction(
            Mono<TransactionRequest> transactionRequest,
            ServerWebExchange exchange) {

        return transactionRequest
                .flatMap(transactionService::create)
                .map(response -> ResponseEntity.status(201).body(response));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> getTransactionById(
            String id,
            ServerWebExchange exchange) {

        return transactionService.findById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<TransactionResponse>>> getAllTransactions(
            ServerWebExchange exchange) {

        Flux<TransactionResponse> responses = transactionService.findAll();
        return Mono.just(ResponseEntity.ok(responses));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> updateTransaction(
            String id,
            Mono<TransactionRequest> transactionRequest,
            ServerWebExchange exchange) {

        return transactionRequest
                .flatMap(req -> transactionService.update(id, req))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteTransaction(
            String id,
            ServerWebExchange exchange) {

        return transactionService.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
