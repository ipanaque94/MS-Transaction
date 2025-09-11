package com.enoc.transaction.infrastructure.messaging.consumer;

import com.enoc.transaction.application.usecase.DebitCardPaymentHandler;
import com.enoc.transaction.events.DebitCardPaymentRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DebitCardPaymentConsumer {

    private final DebitCardPaymentHandler handler;

    @KafkaListener(
            topics = "${kafka.topics.debit-card-payment-requested}",
            groupId = "debit-card-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, DebitCardPaymentRequested> record) {
        DebitCardPaymentRequested event = record.value();
        log.info("Evento recibido: topic=debit.card.payment.requested | key={} | partition={} | offset={} | payload={}",
                record.key(), record.partition(), record.offset(), event);

        try {
            handler.process(event);
        } catch (Exception ex) {
            log.error("❌ Error al procesar pago con tarjeta {}: {}", event.getPaymentId(), ex.getMessage());
        }
    }
}