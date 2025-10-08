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

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import io.github.jeddict.ai.test.TestBase;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class DummyChatModelTest extends TestBase {

    @Test
    public void doChat_returns_provided_message() {
        final DummyChatModel chat = new DummyChatModel();

        ChatRequest chatRequest = ChatRequest.builder().messages(
            UserMessage.from("use mock 'hello world.txt'")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim()).isEqualTo("hello world");
    }

    @Test
    public void doChat_return_the_error_page_if_the_mock_does_not_exist() {
        final DummyChatModel chat = new DummyChatModel();

        ChatRequest chatRequest = ChatRequest.builder().messages(
            UserMessage.from("use mock none.txt")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .startsWith("Oops! Mock file 'src/test/resources/mocks/none.txt' not found.");
    }

    @Test
    public void doChat_returns_the_default_page_if_no_mock_is_given() {
        final DummyChatModel chat = new DummyChatModel();

        ChatRequest chatRequest = ChatRequest.builder().messages(
            UserMessage.from("Hello!")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .startsWith("To use the mock server, send a prompt containing the following instruction:");
    }

}
