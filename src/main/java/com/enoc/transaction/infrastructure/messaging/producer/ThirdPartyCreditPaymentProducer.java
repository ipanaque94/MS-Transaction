package com.enoc.transaction.infrastructure.messaging.producer;

import com.enoc.transaction.events.ThirdPartyCreditPaymentRequested;
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
public class ThirdPartyCreditPaymentProducer {

    @Value("${kafka.topics.third-party-credit-payment-requested}")
    private String topicName;

    private final KafkaTemplate<String, ThirdPartyCreditPaymentRequested> kafkaTemplate;

    public void publish(ThirdPartyCreditPaymentRequested event) {
        ListenableFuture<SendResult<String, ThirdPartyCreditPaymentRequested>> future =
                kafkaTemplate.send(topicName, event.getPaymentId().toString(), event);

        future.addCallback(new ListenableFutureCallback<>() {
            public void onSuccess(SendResult<String, ThirdPartyCreditPaymentRequested> result) {
                log.info("Evento publicado: {} | offset={} | partición={}",
                        event.getPaymentId(), result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            }

            public void onFailure(Throwable ex) {
                log.error("Error al publicar evento {}: {}", event.getPaymentId(), ex.getMessage());
            }
        });
    }
}