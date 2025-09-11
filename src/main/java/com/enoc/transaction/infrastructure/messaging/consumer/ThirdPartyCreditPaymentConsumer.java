package com.enoc.transaction.infrastructure.messaging.consumer;

import com.enoc.transaction.application.usecase.ThirdPartyCreditPaymentHandler;
import com.enoc.transaction.events.ThirdPartyCreditPaymentRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThirdPartyCreditPaymentConsumer {

    private final ThirdPartyCreditPaymentHandler handler;

    @KafkaListener(
            topics = "${kafka.topics.third-party-credit-payment-requested}",
            groupId = "third-party-credit-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, ThirdPartyCreditPaymentRequested> record) {
        ThirdPartyCreditPaymentRequested event = record.value();
        log.info("Evento recibido: topic=third.party.credit.payment.requested | key={} | partition={} | offset={} | payload={}",
                record.key(), record.partition(), record.offset(), event);

        try {
            handler.process(event);
        } catch (Exception ex) {
            log.error("❌ Error al procesar pago de tercero {}: {}", event.getPaymentId(), ex.getMessage());
        }
    }
}