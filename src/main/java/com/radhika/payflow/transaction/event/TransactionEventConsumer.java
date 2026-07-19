package com.radhika.payflow.transaction.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionEventConsumer {
    @KafkaListener(topics = "transaction-events", groupId = "payflow-consumer-group")
    public void consumeEvent(TransactionEvent event) {
         log.info("📩 Received transaction event: {} | Type: {} | Amount: {}",
                event.getTransactionId(), event.getType(), event.getAmount());
    }
}






