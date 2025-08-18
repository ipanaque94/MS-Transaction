package com.enoc.transaction.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.openapitools.model.TransactionRequest;
import org.openapitools.model.TransactionResponse;

public interface TransactionService {
    Mono<TransactionResponse> create(TransactionRequest request);
    Flux<TransactionResponse> findAll();
    Mono<TransactionResponse> findById(String id);
    Mono<TransactionResponse> update(String id, TransactionRequest request);
    Mono<Void> delete(String id);
}

