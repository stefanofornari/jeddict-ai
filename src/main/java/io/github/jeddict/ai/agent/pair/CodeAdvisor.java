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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;



public interface CodeAdvisor {
    public static final String SYSTEM_MESSAGE = """
"You are an expert programmer that can suggest code based on the context of the
program and best practices to write good quality code. Based on user request you will:
- Suggest multiple meaningful and descriptive names for a variable in a given Java class.
- For each name, just give the name of the variable.
- Provide a list with up to 3 names and nothing else as plain lines of text.
""";
    public static final String USER_MESSAGE = """
Based on the below line of code, class it belowngs to and project classes data,
suggest a list of improved names for the variable represented by the
placeholder ${SUGGEST_VAR_NAMES_LIST} in Java Class.
The line of code is: {{line}}
The class is: {{class}}
The project classes are: {{classes}}
""";

    static final Pattern JAVA_ID_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    //
    // NOTE: given the agent returns a List, langchain4j adds to the prompt:
    // \nYou must put every item on a separate line.
    //
    @SystemMessage(SYSTEM_MESSAGE)
    @UserMessage(USER_MESSAGE)
    @Agent("Suggest up to 3 names for a variable")
    List<String> advise(
        @V("line") final String line,
        @V("class") final String code,
        @V("classes") final String classes
    );

    default List<String> suggestVariableNames(
        final String line,
        final String code,
        final String classes
    ) {
        //
        // Get the names and make sure they are valid identifier
        //
        final List<String> names = advise(line, code, classes);
        final List<String> ret = new ArrayList();

        names.forEach((name) -> {
            if (!name.isEmpty()
                && JAVA_ID_PATTERN.matcher(name).matches()
                && !SourceVersion.isKeyword(name)
            ) {
                ret.add(name);
            }
        });

        return ret;
    }

}
