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
package io.github.jeddict.ai.lang;

import dev.langchain4j.model.chat.StreamingChatModel;
import java.time.Duration;
import java.util.Map;

/**
 * A specialized builder interface for constructing StreamingChatLanguageModel instances. This interface extends ChatModelBaseBuilder to provide specific building capabilities for
 * streaming-enabled chat language models. It inherits all configuration methods from the base builder while targeting StreamingChatLanguageModel as the build output type. The
 * streaming functionality enables real-time, token-by-token response generation.
 *
 * @author Francois Steyn
 * @see StreamingChatModel
 * @see ChatModelBaseBuilder
 */
public interface ChatModelStreamingBuilder extends ChatModelBaseBuilder<StreamingChatModel> {

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
    StreamingChatModel build();

}
