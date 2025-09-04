package com.enoc.transaction.repository;

import com.enoc.transaction.model.Transaction;
import java.util.List;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Mono<Long> countByAccountIdAndTypeIn(String accountId, List<String> typeNames);


}

