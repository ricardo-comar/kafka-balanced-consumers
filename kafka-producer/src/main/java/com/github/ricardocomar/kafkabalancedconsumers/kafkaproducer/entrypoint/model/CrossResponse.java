package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model;

import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossResponse {

	private String sender;
	private ResponseMessage response;
}
