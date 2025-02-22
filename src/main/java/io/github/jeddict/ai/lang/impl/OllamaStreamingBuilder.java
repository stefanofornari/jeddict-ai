package io.github.jeddict.ai.lang.impl;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import io.github.jeddict.ai.lang.ChatModelBuilder;
import io.github.jeddict.ai.lang.ChatModelStreamingBuilder;
import java.time.Duration;
import java.util.Map;

/**
 *
 * @author Francois Steyn
 */
public class OllamaStreamingBuilder implements ChatModelStreamingBuilder {

    private final OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder;

    public OllamaStreamingBuilder() {
        builder = OllamaStreamingChatModel.builder();
    }

    @Override
    public ChatModelStreamingBuilder baseUrl(final String baseUrl) {
        builder.baseUrl(baseUrl);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder customHeaders(final Map<String, String> customHeaders) {
        builder.customHeaders(customHeaders);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder apiKey(final String apiKey) {
        //NOOP
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
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder maxOutputTokens(final Integer maxOutputTokens) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder repeatPenalty(final Double repeatPenalty) {
        builder.repeatPenalty(repeatPenalty);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder seed(final Integer seed) {
        builder.seed(seed);
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
        builder.logRequests(logRequests)
                .logResponses(logResponses);
        return this;
    }

    @Override
    public ChatModelStreamingBuilder includeCodeExecutionOutput(final boolean includeCodeExecutionOutput) {
        //NOOP
        return this;
    }

    @Override
    public ChatModelStreamingBuilder allowCodeExecution(final boolean allowCodeExecution) {
        //NOOP
        return this;
    }

    @Override
    public StreamingChatLanguageModel build() {
        return builder.build();
    }
}
