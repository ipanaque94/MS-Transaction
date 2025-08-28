package com.enoc.transaction.service;

import com.enoc.transaction.model.Transaction;
import com.enoc.transaction.repository.TransactionRepository;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.model.TransactionRequest;
import org.openapitools.model.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository repository;

    @Override
    public Mono<TransactionResponse> create(TransactionRequest request) {
        return repository.save(requestToEntity(request))
                .map(this::entityToResponse);
    }

    @Override
    public Flux<TransactionResponse> findAll() {
        return repository.findAll()
                .map(this::entityToResponse);
    }

    @Override
    public Mono<TransactionResponse> findById(String id) {
        return repository.findById(id)
                .map(this::entityToResponse);
    }

    @Override
    public Mono<TransactionResponse> update(String id, TransactionRequest request) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setAmount(request.getAmount());
                    existing.setType(Transaction.TransactionType.valueOf(request.getType().getValue()));
                    existing.setDate(request.getDate().toLocalDateTime());
                    existing.setDescription(request.getDescription().orElse(null));
                    return repository.save(existing);
                })
                .map(this::entityToResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    private Transaction requestToEntity(TransactionRequest dto) {
        return Transaction.builder()
                .productId(dto.getProductId())
                .type(Transaction.TransactionType.valueOf(dto.getType().name()))
                .amount(dto.getAmount())
                .date(dto.getDate().toLocalDateTime())
                .description(dto.getDescription().orElse(null))
                .build();
    }

    private TransactionResponse entityToResponse(Transaction entity) {
        TransactionResponse dto = new TransactionResponse();
        dto.setId(entity.getId());
        dto.setProductId(entity.getProductId());
        dto.setType(TransactionResponse.TypeEnum.valueOf(entity.getType().name()));
        dto.setAmount(entity.getAmount());
        dto.setDate(entity.getDate().atOffset(ZoneOffset.UTC));
        dto.setDescription(JsonNullable.of(entity.getDescription()));
        return dto;
    }
}