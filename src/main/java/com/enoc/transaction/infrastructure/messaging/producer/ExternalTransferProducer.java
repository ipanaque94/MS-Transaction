package com.enoc.transaction.infrastructure.messaging.producer;

import com.enoc.transaction.events.ExternalTransferRequested;
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
public class ExternalTransferProducer {

    @Value("${kafka.topics.external-transfer-requested}")
    private String topicName;

    private final KafkaTemplate<String, ExternalTransferRequested> kafkaTemplate;

    public void publish(ExternalTransferRequested event) {
        ListenableFuture<SendResult<String, ExternalTransferRequested>> future =
                kafkaTemplate.send(topicName, event.getTransferId().toString(), event);

        future.addCallback(new ListenableFutureCallback<>() {
            public void onSuccess(SendResult<String, ExternalTransferRequested> result) {
                log.info("Evento publicado: {} | offset={} | partición={}",
                        event.getTransferId(), result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            }

            public void onFailure(Throwable ex) {
                log.error("Error al publicar evento {}: {}", event.getTransferId(), ex.getMessage());
            }
        });
    }
}
