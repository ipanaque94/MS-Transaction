package com.enoc.transaction.application.event;

import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.events.TransactionCreated;
import com.enoc.transaction.infrastructure.messaging.producer.TransactionCreatedProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final TransactionCreatedProducer producer;

    public void publishCreated(Transaction tx) {
        TransactionCreated event = TransactionCreated.newBuilder()
                .setTransactionId(tx.getId())
                .setCustomerId(tx.getCustomerId())
                .setAmount(tx.getAmount().doubleValue())
                .setType(tx.getType().name())
                .setTimestamp(tx.getCreatedAt().toString())
                .build();

        producer.publish(event);
    }
}