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
package io.github.jeddict.ai.agent.tools.code;

import dev.langchain4j.agent.tool.Tool;

public class ReplaceSnippetByRegexTool extends BaseCodeTool {

    public ReplaceSnippetByRegexTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "ReplaceSnippetByRegexTool_replaceSnippetByRegex",
        value = "Replace parts of a file content by regex pattern with replacement text"
    )
    public String replaceSnippetByRegex(String path, String regexPattern, String replacement)
    throws Exception {
        progress("🔄 Replacing text matching regex '" + regexPattern + "' in file: " + path);

        return withDocument(path, doc -> {
            try {
                String original = doc.getText(0, doc.getLength());
                String modified = original.replaceAll(regexPattern, replacement);

                if (original.equals(modified)) {
                    progress("⚠️ No matches found for regex '" + regexPattern + "' in file: " + path);
                    return "No matches found for pattern.";
                }

                doc.remove(0, doc.getLength());
                doc.insertString(0, modified, null);

                progress("✅ Replacement completed in file: " + path);
                return "File snippet replaced successfully.";
            } catch (Exception e) {
                progress("❌ Replacement failed " + e.getMessage() + " in file: " + path);
                throw e;
            }
        }, true);
    }
}
