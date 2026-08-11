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

package io.github.jeddict.ai.hints;

import io.github.jeddict.ai.components.AssistantChat;
import io.github.jeddict.ai.components.AssistantJeddictBrainListener;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.Test;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class AssistantJeddictBrainListenerTest {

    final AssistantChat assistantChat = new AssistantChat("name", "type", null) {
        @Override
        public void onChatReset() { }

        @Override
        public void onSubmit() { }

        @Override
        public void onPrev() { }

        @Override
        public void onNext() { }

        @Override
        public void onSessionContext() { }

        @Override
        public void addFileTab(FileObject file) {  }

        @Override
        public void clearFileTab() { }

    };

    @Test
    public void initial_canceled_flag_is_false() {
        then(new AssistantJeddictBrainListener(assistantChat).isCanceled()).isFalse();
    }

    @Test
    public void set_canceled_flag() {
        assistantChat.canceled = true;
        then(new AssistantJeddictBrainListener(assistantChat).isCanceled()).isTrue();
    }

}
