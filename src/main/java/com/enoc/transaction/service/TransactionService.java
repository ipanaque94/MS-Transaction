package com.enoc.transaction.service;

import com.enoc.transaction.enums.TransactionType;
import java.util.List;
import org.openapitools.model.TransactionRequest;
import org.openapitools.model.TransactionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<TransactionResponse> create(TransactionRequest request);

    Flux<TransactionResponse> findAll();

    Mono<TransactionResponse> findById(String id);

    Mono<TransactionResponse> update(String id, TransactionRequest request);

    Mono<Void> delete(String id);

    Mono<Long> countByAccountIdAndTypeIn(String accountId, List<TransactionType> types);


}


