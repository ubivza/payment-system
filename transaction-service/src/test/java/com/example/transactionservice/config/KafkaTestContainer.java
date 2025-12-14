package com.example.transactionservice.config;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;

public class KafkaTestContainer {
    @Container
    public static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");
}
