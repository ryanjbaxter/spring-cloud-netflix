/*
 * Copyright 2017-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.netflix.eureka.mcp;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ryan Baxter
 */
@Configuration
public class McpToolsEurekaAutoConfiguration {

	private EurekaInstanceConfigBean eurekaInstanceConfigBean;

	private List<ToolCallbackProvider> toolCallbackProviders;

	McpToolsEurekaAutoConfiguration(EurekaInstanceConfigBean eurekaInstanceConfigBean,
			List<ToolCallbackProvider> toolCallbackProviders) {
		this.eurekaInstanceConfigBean = eurekaInstanceConfigBean;
		this.toolCallbackProviders = toolCallbackProviders;
	}

	@PostConstruct
	public void postConstruct() {
		eurekaInstanceConfigBean.getMetadataMap().put("tools", commaSeparatedListOfToolNames(toolCallbackProviders));
	}

	private String commaSeparatedListOfToolNames(List<ToolCallbackProvider> tools) {
		return toolCallbackProviders.stream()
			.map(pr -> List.of(pr.getToolCallbacks()))
			.flatMap(List::stream)
			.filter(fc -> fc instanceof ToolCallback)
			.map(fc -> ((ToolCallback) fc).getToolDefinition().name())
			.collect(Collectors.joining(","));
	}

}
