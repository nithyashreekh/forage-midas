package com.jpmc.midascore;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jpmc.midascore.foundation.Transaction;

/**
 * Kafka listener that receives Transaction objects and delegates processing to TransactionService.
 */
@Component
public class KafkaListenerService {

    private static final List<Transaction> receivedTransactions =
            Collections.synchronizedList(new ArrayList<>());

    private final TransactionService transactionService;

    public KafkaListenerService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void receive(Transaction transaction) {
        // store raw message for tests / debugging
        receivedTransactions.add(transaction);

        // delegate validation + persistence to the service
        transactionService.process(transaction);

        System.out.println("✅ Received and processed Transaction from Kafka: " + transaction);
    }

    public static List<Transaction> getReceivedTransactions() {
        return receivedTransactions;
    }

    public static void clearReceivedTransactions() {
        receivedTransactions.clear();
    }
}
