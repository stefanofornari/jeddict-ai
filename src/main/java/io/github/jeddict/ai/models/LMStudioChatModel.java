/**
 * Copyright 2025 the original author or authors from the Jeddict project (https://jeddict.github.io/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.jeddict.ai.models;



import static dev.langchain4j.internal.RetryUtils.withRetry;
import dev.langchain4j.model.chat.ChatModel;

import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.internal.OpenAiClient;
import static dev.langchain4j.model.openai.internal.OpenAiUtils.aiMessageFrom;
import static dev.langchain4j.model.openai.internal.OpenAiUtils.finishReasonFrom;
import dev.langchain4j.model.openai.internal.chat.ChatCompletionRequest;
import dev.langchain4j.model.openai.internal.chat.ChatCompletionResponse;
import dev.langchain4j.model.openai.internal.chat.UserMessage;
import dev.langchain4j.model.openai.internal.shared.Usage;
import dev.langchain4j.model.output.TokenUsage;
import java.time.Duration;
import static java.time.Duration.ofSeconds;
import java.util.stream.Collectors;

public class LMStudioChatModel implements ChatModel {

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
                .baseUrl(ensureNotBlank(baseUrl, "baseUrl"))
                .connectTimeout(timeout)
                .readTimeout(timeout)
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
    public ChatResponse doChat(ChatRequest request) {
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(request.messages().stream()
                        .map(msg -> UserMessage.from(msg.toString())) // Convert Langchain4j Message to OpenAI ChatMessage
                        .collect(Collectors.toList()))
                .temperature(temperature)
                .topP(topP)
                .maxTokens(maxTokens)
                .build();

        ChatCompletionResponse response = withRetry(() -> client.chatCompletion(completionRequest).execute(), maxRetries);

        Usage usage = response.usage();

        return ChatResponse.builder()
            .aiMessage(aiMessageFrom(response))
            .tokenUsage(new TokenUsage(usage.promptTokens(), usage.completionTokens()))
            .finishReason(finishReasonFrom(response.choices().get(0).finishReason()))
            .build();
    }
}
