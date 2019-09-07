package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@ConfigurationProperties(prefix = "kafka-consumer")
public class AppProperties {
	
	private Consumer consumer;
	
	@Data @NoArgsConstructor
	public static class Consumer {
		
		private ContainerFactory containerFactory;
		
		@Data @NoArgsConstructor
		public static class ContainerFactory {
			
			Integer concurrency;
			private Properties properties;
			
			@Data @NoArgsConstructor
			public static class Properties {
				
				private Long poolTimeout;
			}
		}
}
}
