package com.enoc.transaction.infrastructure.messaging.consumer;

import com.enoc.transaction.application.usecase.CreditPaymentHandler;
import com.enoc.transaction.events.CreditPaymentRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditPaymentConsumer {

    private final CreditPaymentHandler handler;

    @KafkaListener(topics = "${kafka.topics.credit-payment-requested}", groupId = "credit-consumer-group")
    public void consume(ConsumerRecord<String, CreditPaymentRequested> record) {
        CreditPaymentRequested event = record.value();

        log.info("Evento recibido: topic=credit.payment.requested | key={} | partition={} | offset={} | payload={}",
                record.key(), record.partition(), record.offset(), event);

        try {
            handler.process(event);
        } catch (Exception ex) {
            log.error("❌ Error al procesar evento {}: {}", event.getPaymentId(), ex.getMessage());
        }
    }
}