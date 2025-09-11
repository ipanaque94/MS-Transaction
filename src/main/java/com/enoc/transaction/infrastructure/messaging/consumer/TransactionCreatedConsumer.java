package com.enoc.transaction.infrastructure.messaging.consumer;

import com.enoc.transaction.application.usecase.TransactionCreatedHandler;
import com.enoc.transaction.events.TransactionCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCreatedConsumer {

    private final TransactionCreatedHandler handler;

    @KafkaListener(
            topics = "${kafka.topics.transaction-created}",
            groupId = "transaction-created-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, TransactionCreated> record) {
        TransactionCreated event = record.value();
        log.info("Evento recibido: topic=transaction.created | key={} | partition={} | offset={} | payload={}",
                record.key(), record.partition(), record.offset(), event);

        try {
            handler.process(event); // delega al caso de uso
        } catch (Exception ex) {
            log.error("Error al procesar evento {}: {}", event.getTransactionId(), ex.getMessage());
        }
    }
}
