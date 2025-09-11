package com.enoc.transaction.application.usecase;

import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.StatusEnum;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.repository.TransactionRepository;
import com.enoc.transaction.events.DebitCardPaymentRequested;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DebitCardPaymentHandler {

    private final TransactionRepository repository;

    public void process(DebitCardPaymentRequested event) {
        log.info("📥 Evento recibido: debit.card.payment.requested | ID={}", event.getPaymentId());

        try {
            validate(event);

            Transaction transaction = mapToDomain(event);

            repository.save(transaction);

            log.info("✅ Pago con tarjeta persistido correctamente: {}", transaction.getId());

        } catch (Exception ex) {
            log.error("❌ Error al procesar pago con tarjeta {}: {}", event.getPaymentId(), ex.getMessage());
        }
    }

    private void validate(DebitCardPaymentRequested event) {
        if (event.getPaymentId() == null) {
            throw new IllegalArgumentException("ID de pago nulo");
        }
        if (event.getAmount() <= 0) {
            throw new IllegalArgumentException("Monto inválido: debe ser mayor a cero");
        }
    }

    private Transaction mapToDomain(DebitCardPaymentRequested event) {
        return Transaction.builder()
                .id(event.getPaymentId().toString())
                .amount(BigDecimal.valueOf(event.getAmount()))
                .eventDate(OffsetDateTime.parse(event.getTimestamp().toString()))
                .state(TransactionState.ACTIVE)
                .status(StatusEnum.PENDING)
                .build();
    }
}