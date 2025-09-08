package com.enoc.transaction.infrastructure.service;

import com.enoc.transaction.application.internal.TransactionDomainService;
import com.enoc.transaction.domain.model.Debt;
import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.infrastructure.persistence.DebtRepository;
import com.enoc.transaction.infrastructure.persistence.TransactionRepository;
import com.enoc.transaction.mapper.TransactionMapper;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransactionDomainServiceImpl implements TransactionDomainService {

    private final TransactionRepository transactionRepository;
    private final DebtRepository debtRepository;
    private final TransactionMapper mapper;

    @Override
    public Mono<Transaction> registrar(TransactionRequestDTO dto) {
        Transaction transaction = mapper.mapToEntity(dto); // transforma DTO a entidad
        return transactionRepository.save(transaction);
    }

    @Override
    public Mono<Void> actualizarDeuda(Debt debt, BigDecimal monto) {
        debt.applyPayment(monto); // lógica de negocio encapsulada en la entidad
        return debtRepository.save(debt).then(); // persistencia reactiva
    }

    @Override
    public Mono<Debt> validarDeudaExistente(String dni, BigDecimal monto) {
        return debtRepository.findByDebtorDni(dni)
                .filter(debt -> debt.getAmount().compareTo(monto) >= 0)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No existe deuda suficiente para el monto solicitado")));
    }
}

