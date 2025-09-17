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

public class ReplaceFileContentTool extends BaseCodeTool {

    public ReplaceFileContentTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "ReplaceFileContentTool_replaceFileContent",
        value = "Replace the full content of a file by path with new text"
    )
    public String replaceFileContent(String path, String newContent)
    throws Exception {
        progress("📝 Replacing entire content of file: " + path);

        return withDocument(path, doc -> {
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, newContent, null);

                progress("✅ File content replaced successfully: " + path);
                return "File updated";
            } catch (Exception e) {
                progress("❌ Failed to replace content " + e.getMessage() + " in file: " + path);
                throw e;
            }
        }, true);
    }
}
