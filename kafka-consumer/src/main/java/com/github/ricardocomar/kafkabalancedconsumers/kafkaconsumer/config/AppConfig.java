package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AppConfig {

	@Bean
	public Executor messageProducerSingleThreadExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(1);
		threadPoolTaskExecutor.setQueueCapacity(1_000);
		return threadPoolTaskExecutor;
	}
}
