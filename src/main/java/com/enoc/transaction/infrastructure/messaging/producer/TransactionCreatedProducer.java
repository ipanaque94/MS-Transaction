package com.enoc.transaction.infrastructure.messaging.producer;

import com.enoc.transaction.events.TransactionCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCreatedProducer {

    @Value("${kafka.topics.transaction-created}")
    private String topicName;

    private final KafkaTemplate<String, TransactionCreated> kafkaTemplate;

    public void publish(TransactionCreated event) {
        ListenableFuture<SendResult<String, TransactionCreated>> future =
                kafkaTemplate.send(topicName, event.getTransactionId().toString(), event);

        future.addCallback(new ListenableFutureCallback<>() {
            public void onSuccess(SendResult<String, TransactionCreated> result) {
                log.info("Evento publicado: {} | offset={} | partición={}",
                        event.getTransactionId(), result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            }

            public void onFailure(Throwable ex) {
                log.error("Error al publicar evento {}: {}", event.getTransactionId(), ex.getMessage());
            }
        });
    }
}