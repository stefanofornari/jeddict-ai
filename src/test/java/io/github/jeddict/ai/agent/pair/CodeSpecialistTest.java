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
package io.github.jeddict.ai.agent.pair;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import static io.github.jeddict.ai.agent.pair.PairProgrammerTestBase.TEXT;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class CodeSpecialistTest extends PairProgrammerTestBase {

    @Test
    public void updateMethodFromDevQuery_returns_AI_provided_response_with_and_without_rules() {
        final CodeSpecialist pair = AgenticServices.agentBuilder(CodeSpecialist.class)
            .chatModel(model)
            .build();

        updateMethodFromDevQuery_returns_AI_provided_response(TEXT, "the class", "the method", "no rules", "no rules", pair::updateMethodFromDevQuery);
        updateMethodFromDevQuery_returns_AI_provided_response(TEXT, "the class", "the method", "\n- global rule 1", "no rules", pair::updateMethodFromDevQuery);
        updateMethodFromDevQuery_returns_AI_provided_response(TEXT, "the class", "the method", "no rules", "\n- project rule 1", pair::updateMethodFromDevQuery);
        updateMethodFromDevQuery_returns_AI_provided_response(TEXT, "the class", "the method", "\n- global rule 1", "\n- project rule 1", pair::updateMethodFromDevQuery);
    }

    // --------------------------------------------------------- private methods


    @FunctionalInterface
    protected static interface CodeGenerator {
        String apply(String msg, String code, String method, String globalRules, String projectRules);
    }

    protected void updateMethodFromDevQuery_returns_AI_provided_response(
        final String msg,
        final String code,
        final String method,
        final String globalRules,
        final String projectRules,
        final CodeGenerator generator
    ) {
        //
        // invoke the agent
        //
        generator.apply(msg, code, method, globalRules, projectRules);

        //
        // proper prompt messages has been generated and provided
        //
        final ChatModelRequestContext request = listener.lastRequestContext.get();
        thenMessagesMatch(
                request.chatRequest().messages(),
                CodeSpecialist.SYSTEM_MESSAGE
                        .replace("{{globalRules}}", (globalRules.trim().isEmpty()) ? "no rules" : globalRules)
                        .replace("{{projectRules}}", (projectRules.trim().isEmpty()) ? "no rules" : projectRules),
                CodeSpecialist.USER_MESSAGE
                        .replace("{{userRequest}}", msg)
                        .replace("{{code}}", code)
                        .replace("{{methodCode}}", method)
        );
    }

}
