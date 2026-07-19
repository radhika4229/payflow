package com.radhika.payflow.transaction.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private static final String TOPIC = "transaction-event";
    public void publishEvent(TransactionEvent event) {
            kafkaTemplate.send(TOPIC, event.getTransactionId().toString(), event);
    }
}






