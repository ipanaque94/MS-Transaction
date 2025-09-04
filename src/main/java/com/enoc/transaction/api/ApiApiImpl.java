package com.enoc.transaction.api;

import com.enoc.transaction.controller.TransactionController;
import org.openapitools.api.ApiApi;
import org.openapitools.model.TransactionRequest;
import org.openapitools.model.TransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ApiApiImpl implements ApiApi {

    private final TransactionController controller;

    public ApiApiImpl(TransactionController controller) {
        this.controller = controller;
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> createTransaction(
            Mono<TransactionRequest> transactionRequest,
            ServerWebExchange exchange) {
        return controller.create(transactionRequest);
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> getTransactionById(
            String id,
            ServerWebExchange exchange) {
        return controller.getById(id);
    }

    @Override
    public Mono<ResponseEntity<Flux<TransactionResponse>>> getAllTransactions(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(controller.getAll()));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> updateTransaction(
            String id,
            Mono<TransactionRequest> transactionRequest,
            ServerWebExchange exchange) {
        return controller.update(id, transactionRequest);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteTransaction(
            String id,
            ServerWebExchange exchange) {
        return controller.delete(id);
    }
}


