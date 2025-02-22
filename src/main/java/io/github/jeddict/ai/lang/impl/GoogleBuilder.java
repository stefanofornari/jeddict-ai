package io.github.jeddict.ai.lang.impl;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import io.github.jeddict.ai.lang.ChatModelBuilder;
import java.time.Duration;
import java.util.Map;

/**
 *
 * @author Francois Steyn
 */
public class GoogleBuilder implements ChatModelBuilder {

    private final GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder builder;

    public GoogleBuilder() {
        builder = GoogleAiGeminiChatModel.builder();
    }

    @Override
    public ChatModelBuilder baseUrl(final String baseUrl) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelBuilder customHeaders(final Map<String, String> customHeaders) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelBuilder apiKey(final String apiKey) {
        builder.apiKey(apiKey);
        return this;
    }

    @Override
    public ChatModelBuilder modelName(final String modelName) {
        builder.modelName(modelName);
        return this;
    }

    @Override
    public ChatModelBuilder temperature(final Double temperature) {
        builder.temperature(temperature);
        return this;
    }

    @Override
    public ChatModelBuilder timeout(final Duration timeout) {
        builder.timeout(timeout);
        return this;
    }

    @Override
    public ChatModelBuilder topP(final Double topP) {
        builder.topP(topP);
        return this;
    }

    @Override
    public ChatModelBuilder maxRetries(final Integer maxRetries) {
        builder.maxRetries(maxRetries);
        return this;
    }

    @Override
    public ChatModelBuilder maxOutputTokens(final Integer maxOutputTokens) {
        builder.maxOutputTokens(maxOutputTokens);
        return this;
    }

    @Override
    public ChatModelBuilder repeatPenalty(final Double repeatPenalty) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelBuilder seed(final Integer seed) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelBuilder maxTokens(final Integer maxTokens) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelBuilder maxCompletionTokens(final Integer maxCompletionTokens) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelBuilder topK(final Integer topK) {
        builder.topK(topK);
        return this;
    }

    @Override
    public ChatModelBuilder presencePenalty(final Double presencePenalty) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelBuilder frequencyPenalty(final Double frequencyPenalty) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelBuilder organizationId(final String organizationId) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelBuilder logRequestsResponses(final boolean logRequests, final boolean logResponses) {
        builder.logRequestsAndResponses(logRequests || logResponses);
        return this;
    }

    @Override
    public ChatModelBuilder includeCodeExecutionOutput(final boolean includeCodeExecutionOutput) {
        builder.includeCodeExecutionOutput(includeCodeExecutionOutput);
        return this;
    }

    @Override
    public ChatModelBuilder allowCodeExecution(final boolean allowCodeExecution) {
        builder.allowCodeExecution(allowCodeExecution);
        return this;
    }

    @Override
    public ChatLanguageModel build() {
        return builder.build();
    }
}
