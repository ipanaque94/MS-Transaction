package com.enoc.transaction.application.usecase;

import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.StatusEnum;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.repository.TransactionRepository;
import com.enoc.transaction.events.ExternalTransferRequested;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferHandler {

    private final TransactionRepository repository;

    public void process(ExternalTransferRequested event) {
        log.info("Evento recibido: external.transfer.requested | ID={}", event.getTransferId());

        try {
            validate(event);

            Transaction transaction = mapToDomain(event);

            repository.save(transaction);

            log.info("Transferencia externa persistida correctamente: {}", transaction.getId());

        } catch (Exception ex) {
            log.error("Error al procesar transferencia externa {}: {}", event.getTransferId(), ex.getMessage());
        }
    }

    private void validate(ExternalTransferRequested event) {
        if (event.getTransferId() == null) {
            throw new IllegalArgumentException("ID de transferencia nulo");
        }
        if (event.getAmount() <= 0) {
            throw new IllegalArgumentException("Monto inválido: debe ser mayor a cero");
        }
    }

    private Transaction mapToDomain(ExternalTransferRequested event) {
        return Transaction.builder()
                .id(event.getTransferId().toString())
                .amount(BigDecimal.valueOf(event.getAmount()))
                .eventDate(OffsetDateTime.parse(event.getTimestamp().toString()))
                .state(TransactionState.ACTIVE)
                .status(StatusEnum.PENDING)
                .build();
    }
}