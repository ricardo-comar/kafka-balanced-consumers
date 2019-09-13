package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessResponse {

	private String id;
	private String responseId;
	private Integer duration;
}
