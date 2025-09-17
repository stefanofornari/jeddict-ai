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
import java.util.regex.Pattern;

public class InsertLineAfterMethodTool extends BaseCodeTool {

    private final InsertLineInFileTool insertLineInFileTool;

    public InsertLineAfterMethodTool(final String basedir) {
        super(basedir);
        this.insertLineInFileTool = new InsertLineInFileTool(basedir);
    }

    @Tool(
        name = "InsertLineAfterMethodTool_insertLineAfterMethod",
        value = "Insert a line of text immediately after the specified method in the given file"
    )
    public String insertLineAfterMethod(String path, String methodName, String lineText)
    throws Exception {
        progress("✏️ Inserting line after method '" + methodName + "' in file: " + path);
        String content = withDocument(path, doc -> doc.getText(0, doc.getLength()), false);
        if (content.startsWith("Could not")) {
            progress("❌ Failed to read file: " + path);
            return "Failed to read file: " + content;
        }
        int insertLine = findInsertionLineAfterMethod(content, methodName);
        if (insertLine < 0) {
            progress("⚠️ Method not found: " + methodName + " in file " + path);
            return "Method not found: " + methodName;
        }
        progress("✅ Inserting text at line " + insertLine + " in file: " + path);
        return insertLineInFileTool.insertLineInFile(path, insertLine, lineText);
    }

    private int findInsertionLineAfterMethod(String fileContent, String methodName) {
        String[] lines = fileContent.split("\r?\n");
        int methodStartLine = -1;
        int braceDepth = 0;
        boolean inMethod = false;

        Pattern methodPattern = Pattern.compile("\\b" + Pattern.quote(methodName) + "\\s*\\(.*\\)\\s*\\{\\s*$");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!inMethod) {
                // Detect method declaration line
                if (methodPattern.matcher(line).find()) {
                    inMethod = true;
                    methodStartLine = i;
                    // Count opening brace
                    braceDepth = 1;
                }
            } else {
                // Inside method, track braces to find method end
                braceDepth += countChar(line, '{');
                braceDepth -= countChar(line, '}');
                if (braceDepth == 0) {
                    // Method ends here
                    return i + 1; // return line after method end
                }
            }
        }
        return -1;
    }

    private int countChar(String line, char ch) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ch) {
                count++;
            }
        }
        return count;
    }
}
