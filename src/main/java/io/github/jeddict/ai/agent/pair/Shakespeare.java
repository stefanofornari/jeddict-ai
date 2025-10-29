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

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.github.jeddict.ai.util.AgentUtil;
import org.apache.commons.lang3.StringUtils;


/**
 * The JavadocSpecialist interface provides a structured approach to generating
 * and enhancing Javadoc comments for Java elements such as classes, methods, and members.
 *
 *
 * Core functionality includes:
 * <ul>
 *   <li>Generating new Javadoc comments for classes, methods, and members</li>
 *   <li>Enhancing existing Javadoc comments while preserving original content</li>
 *   <li>Context-aware processing that incorporates both global coding standards and project-specific documentation rules</li>
 *   <li>Rule normalization through {@link AgentUtil} to ensure consistent processing</li>
 * </ul>
 *
 * Implementation notes:
 * <ul>
 *   <li>All methods return Javadoc content wrapped in Javadoc comment boundaries</li>
 *   <li>Empty strings are used when generating new Javadoc (enhancement methods expect existing content)</li>
 * </ul>
 *
 * Typical usage pattern:
 * <pre>
 *   JavadocSpecialist programmer = AgenticServices.agentBuilder(PairProgrammer.Specialist.JAVADOC)
 *                              ...
 *                              .build();
 *   String text = programmer.generate[Class/Method/Member]Javadoc(classCode, globalRules, projectRules);
 *   String text = programmer.enhance[Class/Method/Member]Javadoc(methodCode, existingJavadoc, globalRules, projectRules);
 * </pre>
 *
 */
public interface Shakespeare extends PairProgrammer {
    public static final String SYSTEM_MESSAGE = """
You are tech writer that can:
- review provided text in the context of the java class
- correct and/or improve provided text in the context of the java class
Return only the reviewed text. Do not include any additional details or explanation.
""";

    public static final String USER_MESSAGE = """
{{message}}
The text is: {{text}}
The code is: {{code}}
""";
    public static final String USER_MESSAGE_ENHANCE_TEXT =
        "Enhance the text to be more engaging, clear, and polished." +
        "Ensure the text is well-structured and free of any grammatical errors or awkward phrasing.";

    @SystemMessage(SYSTEM_MESSAGE)
    @UserMessage(USER_MESSAGE)
    @Agent("Generate or enhance javadoccomments based on the class or class member code")
    String review(
        @V("message") final String message,
        @V("text") final String text,
        @V("code") final String code
    );

    default String fixGrammar(
        final String text,
        final String code
    ) {
        LOG.finest(() -> "\ntext: %s\ncode:%s".formatted(StringUtils.abbreviate(text, 80), StringUtils.abbreviate(code, 80)));
        return review("", text, code);
    }

    default String enhanceText(
        final String text,
        final String code
    ) {
        LOG.finest(() -> "\ntext: %s\ncode:%s".formatted(StringUtils.abbreviate(text, 80), StringUtils.abbreviate(code, 80)));
        return review(USER_MESSAGE_ENHANCE_TEXT, text, code);
    }
}
