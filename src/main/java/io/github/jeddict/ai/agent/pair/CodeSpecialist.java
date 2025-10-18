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


/**
 * <p><b>PairProgrammer</b> is an agent designed to facilitate automated generation
 * of rest APIs scaffolding.
 *
 * <p>The interface operates through a structured message-passing system where:
 * <ul>
 *   <li>{@link #SYSTEM_MESSAGE} defines the behavioral constraints and operational guidelines
 *       for the Javadoc generation agent</li>
 *   <li>{@link #USER_MESSAGE} provides the template for user requests containing the target code</li>
 * </ul>
 * </p>
 *
 * <p>Core functionality includes:
 * <ul>
 *   <li>
 * </ul>
 * </p>
 *
 * <p>Typical usage pattern:
 * <pre>
 * PairProgrammer programmer = AgenticServices.agentBuilder(PairProgrammer.class)
 *                             ...
 *                             .build()
 * String text = programmer.generate[Class/Method/Member]Javadoc(classCode, globalRules, projectRules);
 * String text = programmer.enhance[Class/Method/Memebeer]Javadoc(methodCode, existingJavadoc, globalRules, projectRules);
 * </pre>
 * </p>
 *
 * @see https://docs.langchain4j.dev/tutorials/agents
 */
public interface CodeSpecialist {
    public static final String SYSTEM_MESSAGE = """
You are a programmer specialized in writing Java code. Base on user request, you will:
- Write new or enhancing existing code
- Incorporate any specific details or requirements mentioned by the user.
- Include all necessary imports relevant to the enhanced or newly created method.
- Return only the Java method and its necessary imports, without including any class declarations, constructors, or other boilerplate code.
- Format the output as a JSON object with two fields: 'imports' as array (list of necessary imports) and 'methodContent' as text.
  Example output:
  {
    "imports": [
      "io.github.jeddict.ai.agent.pair.RestSpecialits",
      "io.github.jeddict.ai.agent.pair.CodeSpecialits",
      "io.github.jeddict.ai.ReturnType"
    ],
    "methodContent": "public ReturnType method() {\\n // implementation \\n}"
  }
- Ensure the generated methods are unique and not duplicates of existing methods in the class content.
Take into account the following general rules: {{globalRules}}
Take into account the following project rules: {{projectRules}}
""";
    public static final String USER_MESSAGE = """
{{userRequest}}
The class is: {{code}}
The current method code is: {{methodCode}}
""";
    public static final String PROMPT_ENHANCE_METHOD_FROM_METHOD_CODE =
        "Given the following Java class content and Java method content, modify and enhance the method accordingly.";
    public static final String FIX_METHOD_COMPILATION_ERROR =
        "Given the compilation error %s and the following Java class and method content, fix the method so it compiles properly";

    @SystemMessage(SYSTEM_MESSAGE)
    @UserMessage(USER_MESSAGE)
    @Agent("Generate or enhance a method based on the class code and method code")
    String updateMethodFromDevQuery(
        @V("userRequest") final String userRequest,
        @V("code") final String source,
        @V("methodCode") final String methodSource,
        @V("globalRules") final String globalRules,
        @V("projectRules") final String projectRules
    );

    default String enhanceMethodFromMethodContent(
        final String source, final String methodSource, final String globalRules, final String projectRules
    ) {
        return updateMethodFromDevQuery(
            PROMPT_ENHANCE_METHOD_FROM_METHOD_CODE, source, methodSource, globalRules, projectRules
        );
    }

    default String fixMethodCompilationError(
        final String error, final String source, final String methodSource, final String globalRules, final String projectRules
    ) {
        return updateMethodFromDevQuery(
            String.format(FIX_METHOD_COMPILATION_ERROR, error), source, methodSource, globalRules, projectRules
        );
    }
}
