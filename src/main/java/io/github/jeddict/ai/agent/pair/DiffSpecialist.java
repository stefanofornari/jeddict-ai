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
import org.apache.commons.lang3.StringUtils;


/**
 * The Shakespeare interface defines an agent for fixing or enhancing text in
 * java strings.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Reviewing provided text with respect to the associated Java code.</li>
 *   <li>Correcting and improving text to ensure clarity, engagement, and polish.</li>
 * </ul>
 *
 * This interface extends {@link PairProgrammer}, thereby fitting into a broader
 * programming assistance context.
 */
public interface DiffSpecialist extends PairProgrammer {

    public static final String SYSTEM_MESSAGE = """
You are an experience developer specialized in in writing commit messages based on provided git diff output.
Based on the provided diff and an optional reference comment, you can:
- create commit messages that reflect business or domain features rather than technical details like dependency updates or refactoring
- create the following versions of the commit message:
  - Very short
  - Short
  - Medium length
  - Long
  - Descriptive
""";

    public static final String USER_MESSAGE = """
Provide various types of commit messages (very short, short, medium length, long, descriptive)
based on reference comment and changes below.
Reference comment: {{reference}}
{{diff}}
""";

    @SystemMessage(SYSTEM_MESSAGE)
    @UserMessage(USER_MESSAGE)
    @Agent("Suggest commit messages for the given git diff")
    String review(
        @V("reference") final String referenceMessage,
        @V("diff") final String diff
    );

    default String suggestCommitMessages(
        final String diff,
        final String referenceMessage
    ) {
        //
        // NOTE for the reviewer: this functionality is invoked from GenerateCommitMessageAction,
        // which creates a new AssistantChatManager on which askQueryForProjectCommit
        // is called. This means that despite the original code was carrying on
        // pictures and history, those data are always be empty.
        // Therefore I have removed them from here.
        //
        LOG.finest(() -> "\nreferenceMessage: %s\ndiff:%s".formatted(StringUtils.abbreviate(referenceMessage, 80), StringUtils.abbreviate(diff, 80)));
        return review(referenceMessage, diff);
    }
}
