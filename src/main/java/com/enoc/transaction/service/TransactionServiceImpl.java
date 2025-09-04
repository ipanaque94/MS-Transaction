package com.enoc.transaction.service;

import com.enoc.transaction.enums.TransactionType;
import com.enoc.transaction.mapper.TransactionMapper;
import com.enoc.transaction.model.Transaction;
import com.enoc.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.openapitools.model.TransactionRequest;
import org.openapitools.model.TransactionResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {


    private final TransactionRepository repository;
    private final TransactionMapper mapper;

    private static final int FREE_TRANSACTION_LIMIT = 5;
    private static final BigDecimal COMMISSION_AMOUNT = new BigDecimal("2.50");

    @Override
    public Mono<TransactionResponse> create(TransactionRequest request) {
        List<String> commissionableTypeNames = List.of(
                TransactionRequest.TypeEnum.DEPOSIT.name(),
                TransactionRequest.TypeEnum.WITHDRAWAL.name()
        );


        return repository.countByAccountIdAndTypeIn(request.getAccountId(), commissionableTypeNames)
                .flatMap(count -> {
                    Transaction entity = mapper.toEntity(request);

                    boolean isCommissionable = commissionableTypeNames.contains(request.getType().name());
                    entity.setCommissionApplied(
                            isCommissionable && count >= FREE_TRANSACTION_LIMIT
                                    ? COMMISSION_AMOUNT
                                    : BigDecimal.ZERO
                    );

                    return repository.save(entity);
                })
                .map(mapper::toResponse);

    }

    @Override
    public Flux<TransactionResponse> findAll() {
        return repository.findAll()
                .map(mapper::toResponse);
    }

    @Override
    public Mono<TransactionResponse> findById(String id) {
        return repository.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    public Mono<TransactionResponse> update(String id, TransactionRequest request) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setAmount(request.getAmount());
                    existing.setType(TransactionType.TRANSFER_INTERNAL);
                    existing.setDate(request.getDate().toLocalDateTime());
                    existing.setDescription(request.getDescription().orElse(null));
                    return repository.save(existing);
                })
                .map(mapper::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Long> countByAccountIdAndTypeIn(String accountId, List<TransactionType> types) {
        List<String> typeNames = types.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        return repository.countByAccountIdAndTypeIn(accountId, typeNames);
    }


}