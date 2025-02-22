package io.github.jeddict.ai.lang;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import java.time.Duration;
import java.util.Map;

/**
 * A specialized builder interface for constructing StreamingChatLanguageModel instances. This interface extends ChatModelBaseBuilder to provide specific building capabilities for
 * streaming-enabled chat language models. It inherits all configuration methods from the base builder while targeting StreamingChatLanguageModel as the build output type. The
 * streaming functionality enables real-time, token-by-token response generation.
 *
 * @author Francois Steyn
 * @see StreamingChatLanguageModel
 * @see ChatModelBaseBuilder
 */
public interface ChatModelStreamingBuilder extends ChatModelBaseBuilder<StreamingChatLanguageModel> {

    @Override
    ChatModelStreamingBuilder baseUrl(final String baseUrl);

    @Override
    ChatModelStreamingBuilder customHeaders(final Map<String, String> customHeaders);

    @Override
    ChatModelStreamingBuilder apiKey(final String apiKey);

    @Override
    ChatModelStreamingBuilder modelName(final String modelName);

    @Override
    ChatModelStreamingBuilder temperature(final Double temperature);

    @Override
    ChatModelStreamingBuilder timeout(final Duration timeout);

    @Override
    ChatModelStreamingBuilder topP(final Double topP);

    @Override
    ChatModelStreamingBuilder maxRetries(final Integer maxRetries);

    @Override
    ChatModelStreamingBuilder maxOutputTokens(final Integer maxOutputTokens);

    @Override
    ChatModelStreamingBuilder repeatPenalty(final Double repeatPenalty);

    @Override
    ChatModelStreamingBuilder seed(final Integer seed);

    @Override
    ChatModelStreamingBuilder maxTokens(final Integer maxTokens);

    @Override
    ChatModelStreamingBuilder maxCompletionTokens(final Integer maxCompletionTokens);

    @Override
    ChatModelStreamingBuilder topK(final Integer topK);

    @Override
    ChatModelStreamingBuilder presencePenalty(final Double presencePenalty);

    @Override
    ChatModelStreamingBuilder frequencyPenalty(final Double frequencyPenalty);

    @Override
    ChatModelStreamingBuilder organizationId(final String organizationId);

    @Override
    ChatModelStreamingBuilder logRequestsResponses(final boolean logRequests, final boolean logResponses);

    @Override
    ChatModelStreamingBuilder includeCodeExecutionOutput(final boolean includeCodeExecutionOutput);

    @Override
    ChatModelStreamingBuilder allowCodeExecution(final boolean allowCodeExecution);

    @Override
    StreamingChatLanguageModel build();

}
