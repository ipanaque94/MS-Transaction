package com.enoc.transaction.infrastructure.api;

import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import com.enoc.transaction.infrastructure.rest.TransactionController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ApiApiImpl {

    private final TransactionController controller;

    public ApiApiImpl(TransactionController controller) {
        this.controller = controller;
    }

    public Mono<ResponseEntity<TransactionResponseDto>> createTransaction(
            Mono<TransactionRequestDTO> transactionRequest,
            ServerWebExchange exchange) {
        return controller.create(transactionRequest);
    }

    public Mono<ResponseEntity<TransactionResponseDto>> getTransactionById(
            String id,
            ServerWebExchange exchange) {
        return controller.getById(id);
    }

    public Mono<ResponseEntity<Flux<TransactionResponseDto>>> getAllTransactions(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(controller.getAll()));
    }

    public Mono<ResponseEntity<TransactionResponseDto>> updateTransaction(
            String id,
            Mono<TransactionRequestDTO> transactionRequest,
            ServerWebExchange exchange) {
        return controller.update(id, transactionRequest);
    }

    public Mono<ResponseEntity<Void>> deleteTransaction(
            String id,
            ServerWebExchange exchange) {
        return controller.delete(id);
    }
}