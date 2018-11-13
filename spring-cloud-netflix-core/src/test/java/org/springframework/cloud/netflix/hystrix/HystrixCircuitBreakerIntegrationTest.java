/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.netflix.hystrix;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.HystrixCommandProperties;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Ryan Baxter
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = HystrixCircuitBreakerIntegrationTest.Application.class)
@DirtiesContext
public class HystrixCircuitBreakerIntegrationTest {

	@Configuration
	@EnableAutoConfiguration
	@RestController
	protected static class Application {
		@GetMapping("/slow")
		public String slow() throws InterruptedException {
			Thread.sleep(3000);
			return "slow";
		}

		@GetMapping("/normal")
		public String normal() {
			return "normal";
		}

		@Bean
		public HystrixCircuitBreakerConfigFactory configFactory() {
			return new HystrixCircuitBreakerConfigFactory() {
				@Override
				public HystrixCommandProperties.Setter get(String id) {
					return HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(1000);
				}
			};
		}

		@Service
		public static class DemoControllerService {
			private TestRestTemplate rest;
			private CircuitBreakerBuilder cbBuilder;

			public DemoControllerService(TestRestTemplate rest, CircuitBreakerBuilder cbBuilder) {
				this.rest = rest;
				this.cbBuilder = cbBuilder;
			}

			public String slow() {
				return cbBuilder.id("slow").build().run(() -> rest.getForObject("/slow", String.class), t -> "fallback");
			}

			public String normal() {
				return cbBuilder.id("normal").build().run(() -> rest.getForObject("/normal", String.class), t -> "fallback");
			}
		}
	}

	@Autowired
	Application.DemoControllerService service;

	@Test
	public void testSlow() {
		assertEquals("fallback", service.slow());
	}

	@Test
	public void testNormal() {
		assertEquals("normal", service.normal());
	}
}
