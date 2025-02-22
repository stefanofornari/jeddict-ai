package io.github.jeddict.ai.lang.impl;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import io.github.jeddict.ai.lang.ChatModelStreamingBuilder;
import java.time.Duration;
import java.util.Map;

/**
 *
 * @author Francois Steyn
 */
public class GoogleStreamingBuilder implements ChatModelStreamingBuilder {

    private final GoogleAiGeminiStreamingChatModel.GoogleAiGeminiStreamingChatModelBuilder builder;

    public GoogleStreamingBuilder() {
        builder = GoogleAiGeminiStreamingChatModel.builder();
    }

    @Override
    public ChatModelStreamingBuilder baseUrl(final String baseUrl) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder customHeaders(final Map<String, String> customHeaders) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder apiKey(final String apiKey) {
        builder.apiKey(apiKey);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder modelName(final String modelName) {
        builder.modelName(modelName);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder temperature(final Double temperature) {
        builder.temperature(temperature);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder timeout(final Duration timeout) {
        builder.timeout(timeout);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder topP(final Double topP) {
        builder.topP(topP);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder maxRetries(final Integer maxRetries) {
        builder.maxRetries(maxRetries);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder maxOutputTokens(final Integer maxOutputTokens) {
        builder.maxOutputTokens(maxOutputTokens);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder repeatPenalty(final Double repeatPenalty) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder seed(final Integer seed) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder maxTokens(final Integer maxTokens) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder maxCompletionTokens(final Integer maxCompletionTokens) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder topK(final Integer topK) {
        builder.topK(topK);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder presencePenalty(final Double presencePenalty) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder frequencyPenalty(final Double frequencyPenalty) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder organizationId(final String organizationId) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder logRequestsResponses(final boolean logRequests, final boolean logResponses) {
        builder.logRequestsAndResponses(logRequests || logResponses);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder includeCodeExecutionOutput(final boolean includeCodeExecutionOutput) {
        builder.includeCodeExecutionOutput(includeCodeExecutionOutput);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder allowCodeExecution(final boolean allowCodeExecution) {
        builder.allowCodeExecution(allowCodeExecution);
        return this;
    }

    @Override
    public StreamingChatLanguageModel build() {
        return builder.build();
    }

}
