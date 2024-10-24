/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.jeddict.ai.models;

import dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionRequest;
import dev.ai4j.openai4j.chat.ChatCompletionResponse;
import dev.ai4j.openai4j.shared.Usage;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import static dev.langchain4j.internal.RetryUtils.withRetry;
import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import dev.langchain4j.model.chat.ChatLanguageModel;
import static dev.langchain4j.model.openai.InternalOpenAiHelper.aiMessageFrom;
import static dev.langchain4j.model.openai.InternalOpenAiHelper.finishReasonFrom;
import static dev.langchain4j.model.openai.InternalOpenAiHelper.toFunctions;
import static dev.langchain4j.model.openai.InternalOpenAiHelper.toOpenAiMessages;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import java.time.Duration;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import java.util.List;

public class LMStudioChatModel implements ChatLanguageModel {

    public static final String LMSTUDIO_MODEL_URL = "http://localhost:1234/v1/";
    private final OpenAiClient client;
    private final String modelName;
    private final Double temperature;
    private final Double topP;
    private final Integer maxTokens;
    private final Integer maxRetries;

    private LMStudioChatModel(String baseUrl,
            String modelName,
            Double temperature,
            Double topP,
            Integer maxTokens,
            Duration timeout,
            Integer maxRetries,
            Boolean logRequests,
            Boolean logResponses) {

        temperature = temperature == null ? 0.7 : temperature;
        timeout = timeout == null ? ofSeconds(60) : timeout;
        maxRetries = maxRetries == null ? 3 : maxRetries;

        this.client = OpenAiClient.builder()
                .openAiApiKey("ignored")
                .baseUrl(ensureNotBlank(baseUrl, "baseUrl"))
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
        this.modelName = ensureNotBlank(modelName, "modelName");
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = maxTokens;
        this.maxRetries = maxRetries;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String baseUrl;
        private String modelName;
        private Double temperature;
        private Double topP;
        private Integer maxTokens;
        private Duration timeout;
        private Integer maxRetries;
        private Boolean logRequests;
        private Boolean logResponses;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder logRequests(Boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        public Builder logResponses(Boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        public LMStudioChatModel build() {
            return new LMStudioChatModel(baseUrl, modelName, temperature, topP, maxTokens, timeout, maxRetries, logRequests, logResponses);
        }
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        return generate(messages, null, null);
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications) {
        return generate(messages, toolSpecifications, null);
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification toolSpecification) {
        return generate(messages, singletonList(toolSpecification), toolSpecification);
    }

    private Response<AiMessage> generate(List<ChatMessage> messages,
            List<ToolSpecification> toolSpecifications,
            ToolSpecification toolThatMustBeExecuted
    ) {
        ChatCompletionRequest.Builder requestBuilder = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(toOpenAiMessages(messages))
                .temperature(temperature)
                .topP(topP)
                .maxTokens(maxTokens);

        if (toolSpecifications != null && !toolSpecifications.isEmpty()) {
            requestBuilder.functions(toFunctions(toolSpecifications));
        }
        if (toolThatMustBeExecuted != null) {
            requestBuilder.functionCall(toolThatMustBeExecuted.name());
        }

        ChatCompletionRequest request = requestBuilder.build();

        ChatCompletionResponse response = withRetry(() -> client.chatCompletion(request).execute(), maxRetries);

        Usage usage = response.usage();

        return Response.from(
                aiMessageFrom(response),
                new TokenUsage(usage.promptTokens(), usage.completionTokens()),
                finishReasonFrom(response.choices().get(0).finishReason())
        );
    }
}
