package com.enoc.transaction.infrastructure.messaging.producer;

import com.enoc.transaction.events.CreditPaymentRequested;
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
public class CreditPaymentProducer {

    @Value("${kafka.topics.credit-payment-requested}")
    private String topicName;

    private final KafkaTemplate<String, CreditPaymentRequested> kafkaTemplate;

    public void publish(CreditPaymentRequested event) {
        ListenableFuture<SendResult<String, CreditPaymentRequested>> future =
                kafkaTemplate.send(topicName, event.getPaymentId().toString(), event);

        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, CreditPaymentRequested> result) {
                log.info("Evento publicado: {} | offset={} | partición={}",
                        event.getPaymentId(),
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Error al publicar evento {}: {}", event.getPaymentId(), ex.getMessage());
            }
        });
    }
}