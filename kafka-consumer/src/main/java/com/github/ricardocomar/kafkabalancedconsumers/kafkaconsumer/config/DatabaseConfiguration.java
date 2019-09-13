package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableJpaRepositories(basePackages = "com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository")
@EntityScan("com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository.entity")
@EnableTransactionManagement
public class DatabaseConfiguration {
}
