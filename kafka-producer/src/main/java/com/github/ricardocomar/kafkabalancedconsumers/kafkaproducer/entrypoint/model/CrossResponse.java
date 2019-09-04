package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossResponse {

	private String id;
	private String origin;
	private String sender;
	private String responseId;
	private Long duration;
}
