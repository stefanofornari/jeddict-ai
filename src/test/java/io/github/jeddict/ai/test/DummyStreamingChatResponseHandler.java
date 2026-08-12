/*
 * Copyright 2026 the original author or authors from the LLMTooliy project
 * (https://stefanofornari.github.io/llm-toolify).
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

package io.github.jeddict.ai.test;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DummyStreamingChatResponseHandler implements StreamingChatResponseHandler {
    public final List<String> messages = new ArrayList();

    @Override
    public void onPartialResponse(final String partialResponse) {
        messages.add(partialResponse);
    }

    @Override
    public void onCompleteResponse(final ChatResponse res) {
        // .trim() to make it platform independent (i.e. \n vs \r\n)
        messages.add(res.aiMessage().text().trim());
    }

    @Override
    public void onError(Throwable thrwbl) {}
}
