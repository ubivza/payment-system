package com.example.transactionservice.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    @Value("${kafka.topic.transaction-service.transaction.insert.name}")
    private String writeTopicName;
    @Value("${kafka.topic.transaction-service.transaction.read.name}")
    private String readTopicName;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topicTransactionInsert() {
        return new NewTopic(writeTopicName, 1, (short) 1);
    }

    @Bean
    public NewTopic topicTransactionRead() {
        return new NewTopic(readTopicName, 1, (short) 1);
    }
}
