package com.enoc.transaction.application.internal;

import com.enoc.transaction.domain.model.Debt;
import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import java.math.BigDecimal;
import reactor.core.publisher.Mono;

public interface TransactionDomainService {
    Mono<Transaction> registrar(TransactionRequestDTO dto);

    Mono<Void> actualizarDeuda(Debt debt, BigDecimal monto);

    Mono<Debt> validarDeudaExistente(String dni, BigDecimal monto);

}
