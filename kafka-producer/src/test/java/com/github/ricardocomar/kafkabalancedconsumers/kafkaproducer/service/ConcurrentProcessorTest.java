package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service;

import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.kafka.common.errors.TimeoutException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.exception.UnavailableResponseException;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
		ConcurrentProcessor.class }, initializers = ConfigFileApplicationContextInitializer.class)
public class ConcurrentProcessorTest {

	@MockBean
	private KafkaTemplate<String, RequestMessage> template;

	@Autowired
	private ConcurrentProcessor processor;

	@Before
	public void before() {
		processor.waitTimeout = 200L;
		Mockito.when(template.send(Mockito.anyString(), Mockito.any(RequestMessage.class)))
				.thenReturn(Mockito.mock(ListenableFuture.class));
	}

	@Test
	public void testRelease() {
		final ResponseMessage response = ResponseMessage.builder().id("123").build();
		final ResponseMessage expected = ResponseMessage.builder().id("123").duration(100L).build();

		new Thread(new Runnable() {
			public void run() {
				try {
					ResponseMessage resp = processor.handle(RequestMessage.builder().id("123").build());
					response.setDuration(resp.getDuration());
				} catch (UnavailableResponseException e) {
					e.printStackTrace();
				}
			}
		}).start();
		sleep(50);

		processor.notifyResponse(expected);
		sleep(50);

		assertThat(response, Matchers.equalTo(expected));
	}

	@Test
	public void testTimeout() {
		final ResponseMessage response = ResponseMessage.builder().id("456").build();

		new Thread(new Runnable() {
			public void run() {
				try {
					ResponseMessage resp = processor.handle(RequestMessage.builder().id("456").build());
					response.setDuration(resp.getDuration());
				} catch (UnavailableResponseException e) {
					e.printStackTrace();
				}
			}
		}).start();
		sleep(350);

		assertThat(response.getDuration(), Matchers.nullValue());
	}

	@Test
	public void testConcurrent() {
		String successId = "AAA", timeoutId = "XXX";
		final ResponseMessage responseSuccess = ResponseMessage.builder().id(successId).build();
		final ResponseMessage expectedSuccess = ResponseMessage.builder().id(successId).duration(100L).build();
		final ResponseMessage responseTimeout = ResponseMessage.builder().id(timeoutId).build();
		final ResponseMessage expectedTimeout = ResponseMessage.builder().id(timeoutId).build();

		new Thread(new Runnable() {
			public void run() {
				try {
					ResponseMessage resp = processor.handle(RequestMessage.builder().id(successId).build());
					responseSuccess.setDuration(resp.getDuration());
				} catch (UnavailableResponseException e) {
					e.printStackTrace();
				}
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					ResponseMessage resp = processor.handle(RequestMessage.builder().id(timeoutId).build());
					responseTimeout.setDuration(resp.getDuration());
				} catch (UnavailableResponseException e) {
					e.printStackTrace();
				}
			}
		}).start();

		sleep(50);
		processor.notifyResponse(expectedSuccess);
		sleep(250);

		assertThat(responseSuccess, Matchers.equalTo(expectedSuccess));
		assertThat(responseTimeout, Matchers.equalTo(expectedTimeout));
	}

	private void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
		}
	}

}
