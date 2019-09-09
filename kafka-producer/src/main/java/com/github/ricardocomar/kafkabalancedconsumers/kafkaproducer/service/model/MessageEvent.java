package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.model;

import org.springframework.context.ApplicationEvent;

import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

public class MessageEvent extends ApplicationEvent {

	public MessageEvent(ResponseMessage source) {
		super(source);
	}
	
	public ResponseMessage getResponse() {
		return super.getSource() != null ? (ResponseMessage) super.getSource() : null;
	}

	private static final long serialVersionUID = 8366022661649087L;

}
