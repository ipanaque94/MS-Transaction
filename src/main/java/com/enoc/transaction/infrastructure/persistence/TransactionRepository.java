package com.enoc.transaction.infrastructure.persistence;


import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.model.enums.TransactionType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

    Mono<Long> countByAccountIdAndTypeIn(String accountId, List<String> typeNames);

    Mono<Transaction> findByIdAndState(String id, TransactionState state);

    Mono<Boolean> existsByCustomerIdAndTypeAndDueDateBeforeAndState(
            String customerId,
            TransactionType type,
            LocalDate date,
            TransactionState state
    );

    Flux<Transaction> findByCardIdAndStateOrderByCreatedAtDesc(String cardId, TransactionState state);

    Flux<Transaction> findTop10ByCustomerIdAndStateOrderByCreatedAtDesc(String customerId, TransactionState state);

    Flux<Transaction> findByCustomerIdAndState(String customerId, TransactionState state);

    Flux<Transaction> findByProductIdAndState(String productId, TransactionState state);

    Mono<Transaction> findTopByCustomerIdAndStateOrderByCreatedAtDesc(String customerId, TransactionState state);

    Flux<Transaction> findByCreatedAtBetweenAndState(OffsetDateTime start, OffsetDateTime end, TransactionState state);


}

