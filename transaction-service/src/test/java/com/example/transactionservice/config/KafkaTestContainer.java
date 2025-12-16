package com.example.transactionservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaTestContainer {
    @Container
    public static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    public static KafkaConsumer<String, Object> getKafkaConsumerForTopic(String topic, Class<?> valueType) {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.currentTimeMillis());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType.getName());

        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(List.of(topic));
        return consumer;
    }

    public static KafkaProducer<String, Object> getKafkaProducerForTopic(String topic, Class<?> valueType) throws ExecutionException, InterruptedException {
        Properties producerProps = new Properties();
        producerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.currentTimeMillis());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        producerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType.getName());

        KafkaProducer<String, Object> producer = new KafkaProducer<>(producerProps);
        return producer;
    }
}
