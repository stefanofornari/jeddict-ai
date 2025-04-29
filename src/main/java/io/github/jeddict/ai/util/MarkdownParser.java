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
package io.github.jeddict.ai.util;

import java.util.*;
import java.util.regex.*;

public class MarkdownParser {
    static class MarkdownBlock {
        String type; // "text" or a language like "java", "bash", etc.
        String content;

        public MarkdownBlock(String type, String content) {
            this.type = type;
            this.content = content;
        }

        @Override
        public String toString() {
            return "Type: " + type + "\nContent:\n" + content + "\n";
        }
    }

    public static void main(String[] args) {
        String input = """
            Here is:

            ```java
            System.out
            ```

            Docs:

            ```markdown
            # JDK Setup Guide

            Use the package manager to install the JDK. For example, on Ubuntu, you can run:
               ```bash
               sudo apt update
               sudo apt install openjdk-11-jdk
               ```

            Edit the `.bash_profile`, `.bashrc`, or `.zshrc` file in your home directory:
               ```bash
               nano ~/.bash_profile
               ```
            ```

            ```shell 
            java -version
            ```

            You have successfully set up the JDK on your system! You can now start developing Java applications.
            """;

        List<MarkdownBlock> blocks = parseMarkdown(input);
        for (MarkdownBlock block : blocks) {
            System.out.println("=== BLOCK ===");
            System.out.println(block);
        }
    }

    public static List<MarkdownBlock> parseMarkdown(String text) {
        List<MarkdownBlock> result = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean insideCodeBlock = false;
        String currentFence = null;
        String codeType = null;

        Scanner scanner = new Scanner(text);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher fenceMatcher = Pattern.compile("^(```+)(\\s*\\w+)?\\s*$").matcher(line);

            if (fenceMatcher.matches()) {
                String fence = fenceMatcher.group(1);
                String lang = fenceMatcher.group(2) != null ? fenceMatcher.group(2).trim() : "";

                if (!insideCodeBlock) {
                    // Starting code block
                    if (buffer.length() > 0) {
                        result.add(new MarkdownBlock("text", buffer.toString().trim()));
                        buffer.setLength(0);
                    }
                    insideCodeBlock = true;
                    currentFence = fence;
                    codeType = lang.isEmpty() ? "code" : lang;
                } else if (line.startsWith(currentFence)) {
                    // Ending code block
                    insideCodeBlock = false;
                    result.add(new MarkdownBlock(codeType, buffer.toString().trim()));
                    buffer.setLength(0);
                    codeType = null;
                }
                // Skip the fence line itself
            } else {
                buffer.append(line).append("\n");
            }
        }

        if (buffer.length() > 0) {
            result.add(new MarkdownBlock(insideCodeBlock ? codeType : "text", buffer.toString().trim()));
        }

        return result;
    }
}
