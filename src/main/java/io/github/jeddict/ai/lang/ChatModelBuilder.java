package io.github.jeddict.ai.lang;

import dev.langchain4j.model.chat.ChatLanguageModel;
import java.time.Duration;
import java.util.Map;

/**
 * A specialized builder interface for constructing ChatLanguageModel instances. This interface extends ChatModelBaseBuilder to provide specific building capabilities for
 * chat-based language models. It inherits all configuration methods from the base builder while specifically targeting ChatLanguageModel as the build output type.
 *
 * @author Francois Steyn
 * @see ChatLanguageModel
 * @see ChatModelBaseBuilder
 */
public interface ChatModelBuilder extends ChatModelBaseBuilder<ChatLanguageModel> {

    @Override
    ChatModelBuilder baseUrl(final String baseUrl);

    @Override
    ChatModelBuilder customHeaders(final Map<String, String> customHeaders);

    @Override
    ChatModelBuilder apiKey(final String apiKey);

    @Override
    ChatModelBuilder modelName(final String modelName);

    @Override
    ChatModelBuilder temperature(final Double temperature);

    @Override
    ChatModelBuilder timeout(final Duration timeout);

    @Override
    ChatModelBuilder topP(final Double topP);

    @Override
    ChatModelBuilder maxRetries(final Integer maxRetries);

    @Override
    ChatModelBuilder maxOutputTokens(final Integer maxOutputTokens);

    @Override
    ChatModelBuilder repeatPenalty(final Double repeatPenalty);

    @Override
    ChatModelBuilder seed(final Integer seed);

    @Override
    ChatModelBuilder maxTokens(final Integer maxTokens);

    @Override
    ChatModelBuilder maxCompletionTokens(final Integer maxCompletionTokens);

    @Override
    ChatModelBuilder topK(final Integer topK);

    @Override
    ChatModelBuilder presencePenalty(final Double presencePenalty);

    @Override
    ChatModelBuilder frequencyPenalty(final Double frequencyPenalty);

    @Override
    ChatModelBuilder organizationId(final String organizationId);

    @Override
    ChatModelBuilder logRequestsResponses(final boolean logRequests, final boolean logResponses);

    @Override
    ChatModelBuilder includeCodeExecutionOutput(final boolean includeCodeExecutionOutput);

    @Override
    ChatModelBuilder allowCodeExecution(final boolean allowCodeExecution);

    @Override
    ChatLanguageModel build();

}
