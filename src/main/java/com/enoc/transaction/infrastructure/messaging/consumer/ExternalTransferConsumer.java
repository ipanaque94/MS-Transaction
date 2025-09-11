package com.enoc.transaction.infrastructure.messaging.consumer;

import com.enoc.transaction.application.usecase.ExternalTransferHandler;
import com.enoc.transaction.events.ExternalTransferRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferConsumer {

    private final ExternalTransferHandler handler;

    @KafkaListener(
            topics = "${kafka.topics.external-transfer-requested}",
            groupId = "external-transfer-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, ExternalTransferRequested> record) {
        ExternalTransferRequested event = record.value();
        log.info("Evento recibido: topic=external.transfer.requested | key={} | partition={} | offset={} | payload={}",
                record.key(), record.partition(), record.offset(), event);

        try {
            handler.process(event);
        } catch (Exception ex) {
            log.error("❌ Error al procesar transferencia externa {}: {}", event.getTransferId(), ex.getMessage());
        }
    }
}