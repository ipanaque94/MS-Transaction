package com.enoc.transaction.infrastructure.messaging.consumer;

import com.enoc.transaction.application.usecase.OrderedDebitWithdrawalHandler;
import com.enoc.transaction.events.OrderedDebitWithdrawalRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderedDebitWithdrawalConsumer {

    private final OrderedDebitWithdrawalHandler handler;

    @KafkaListener(
            topics = "${kafka.topics.ordered-debit-withdrawal-requested}",
            groupId = "ordered-debit-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, OrderedDebitWithdrawalRequested> record) {
        OrderedDebitWithdrawalRequested event = record.value();
        log.info("Evento recibido: topic=ordered.debit.withdrawal.requested | key={} | partition={} | offset={} | payload={}",
                record.key(), record.partition(), record.offset(), event);

        try {
            handler.process(event);
        } catch (Exception ex) {
            log.error("❌ Error al procesar retiro programado {}: {}", event.getWithdrawalId(), ex.getMessage());
        }
    }
}