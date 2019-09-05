package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Value("${kafkaConsummer.instance_id}")
	private String instanceId;
	
	@Bean(name = "instanceId")
	public String instanceId() {
		return instanceId;
	}
}
