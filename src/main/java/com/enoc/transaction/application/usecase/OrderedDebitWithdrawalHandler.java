package com.enoc.transaction.application.usecase;

import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.StatusEnum;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.repository.TransactionRepository;
import com.enoc.transaction.events.OrderedDebitWithdrawalRequested;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderedDebitWithdrawalHandler {

    private final TransactionRepository repository;

    public void process(OrderedDebitWithdrawalRequested event) {
        log.info("📥 Evento recibido: ordered.debit.withdrawal.requested | ID={}", event.getWithdrawalId());

        try {
            validate(event);

            Transaction transaction = mapToDomain(event);

            repository.save(transaction);

            log.info("✅ Retiro programado persistido correctamente: {}", transaction.getId());

        } catch (Exception ex) {
            log.error("❌ Error al procesar retiro programado {}: {}", event.getWithdrawalId(), ex.getMessage());
        }
    }

    private void validate(OrderedDebitWithdrawalRequested event) {
        if (event.getWithdrawalId() == null) {
            throw new IllegalArgumentException("ID de retiro nulo");
        }
        if (event.getAmount() <= 0) {
            throw new IllegalArgumentException("Monto inválido: debe ser mayor a cero");
        }
    }

    private Transaction mapToDomain(OrderedDebitWithdrawalRequested event) {
        return Transaction.builder()
                .id(event.getWithdrawalId().toString())
                .amount(BigDecimal.valueOf(event.getAmount()))
                .eventDate(OffsetDateTime.parse(event.getTimestamp().toString()))
                .state(TransactionState.ACTIVE)
                .status(StatusEnum.PENDING)
                .build();
    }
}