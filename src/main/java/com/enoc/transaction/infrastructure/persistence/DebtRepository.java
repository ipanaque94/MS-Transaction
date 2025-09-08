package com.enoc.transaction.infrastructure.persistence;

import com.enoc.transaction.domain.model.Debt;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DebtRepository extends ReactiveCrudRepository<Debt, String> {
    Mono<Debt> findByDebtorDni(String dni);
}

