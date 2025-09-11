package com.enoc.transaction.application.usecase;

import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.StatusEnum;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.repository.TransactionRepository;
import com.enoc.transaction.events.TransactionCreated;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCreatedHandler {

    private final TransactionRepository repository;

    public void process(TransactionCreated event) {
        log.info("📥 Evento recibido: transaction.created | ID={}", event.getTransactionId());

        try {
            validate(event);

            Transaction transaction = mapToDomain(event);

            repository.save(transaction);

            log.info("✅ Transacción persistida correctamente: {}", transaction.getId());

        } catch (Exception ex) {
            log.error("❌ Error al procesar transacción {}: {}", event.getTransactionId(), ex.getMessage());
        }
    }

    private void validate(TransactionCreated event) {
        if (event.getTransactionId() == null) {
            throw new IllegalArgumentException("ID de transacción nulo");
        }
        if (event.getAmount() <= 0) {
            throw new IllegalArgumentException("Monto inválido: debe ser mayor a cero");
        }
    }

    private Transaction mapToDomain(TransactionCreated event) {
        return Transaction.builder()
                .id(event.getTransactionId().toString())
                .amount(BigDecimal.valueOf(event.getAmount()))
                .eventDate(OffsetDateTime.parse(event.getTimestamp().toString()))
                .state(TransactionState.ACTIVE)
                .status(StatusEnum.PENDING)
                .build();
    }
}