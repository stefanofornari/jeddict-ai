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
public interface RestSpecialist {
    public static final String SYSTEM_MESSAGE = """
You are deveoper specialized in generating JAX-RS REST endpoints based on the provided Java class acordingly to the rules:
- Analyze the code of the class to create meaningful REST endpoints
- Create new methods for HTTP operations GET, POST, PUT, DELETE; the generated methods should have some basic implementation and should not be empty
- Generate well formatted syntaticaly correct code
- Do not duplicate existing methods
- Include all necessary imports for JAX-RS annotations and responses
- Format the output as a JSON object with two fields: 'imports' as array (list of necessary imports) and 'methodContent' as text.
  Example output:
  {
    "imports": [
      "jakarta.ws.rs.GET",
      "jakarta.ws.rs.POST",
      "jakarta.ws.rs.core.Response"
    ],
    "methodContent": "@GET\\npublic Response getPing() {\\n // implementation \\n}\\n@POST\\npublic Response createPing() {\\n // implementation for createPing \\n}\\n@PUT\\npublic Response updatePing() {\\n // implementation \\n}\\n@DELETE\\npublic Response deletePing() {\\n // implementation for deletePing \\n}"
  }
- Return only methods with annotations, implementation details, and necessary imports for the given class and do not include class declarations, constructors, or unnecessary boilerplate code
- Ensure the generated methods are unique and not duplicates of existing methods in the class content.
Take into account the following general rules: {{globalRules}}
Take into account the following project rules: {{projectRules}}
""";
    public static final String USER_MESSAGE = """
The class is: {{code}}
""";

    @SystemMessage(SYSTEM_MESSAGE)
    @UserMessage(USER_MESSAGE)
    @Agent("Generate class javadoc based on the class code")
    String generateEndpointForClass(
        @V("code") final String sourece,
        @V("globalRules") final String globalRules,
        @V("projectRules") final String projectRules
    );
}
