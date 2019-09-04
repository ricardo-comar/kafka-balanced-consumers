package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseMessage {

	private String origin;
	private Long slept;
}
