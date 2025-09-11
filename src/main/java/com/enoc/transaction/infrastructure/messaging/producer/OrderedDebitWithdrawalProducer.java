package com.enoc.transaction.infrastructure.messaging.producer;

import com.enoc.transaction.events.OrderedDebitWithdrawalRequested;
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
public class OrderedDebitWithdrawalProducer {

    @Value("${kafka.topics.ordered-debit-withdrawal-requested}")
    private String topicName;

    private final KafkaTemplate<String, OrderedDebitWithdrawalRequested> kafkaTemplate;

    public void publish(OrderedDebitWithdrawalRequested event) {
        ListenableFuture<SendResult<String, OrderedDebitWithdrawalRequested>> future =
                kafkaTemplate.send(topicName, event.getWithdrawalId().toString(), event);

        future.addCallback(new ListenableFutureCallback<>() {
            public void onSuccess(SendResult<String, OrderedDebitWithdrawalRequested> result) {
                log.info("Evento publicado: {} | offset={} | partición={}",
                        event.getWithdrawalId(), result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            }

            public void onFailure(Throwable ex) {
                log.error("Error al publicar evento {}: {}", event.getWithdrawalId(), ex.getMessage());
            }
        });
    }
}