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
import javax.swing.text.Element;

public class InsertLineInFileTool extends BaseCodeTool {

    public InsertLineInFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "InsertLineInFileTool_insertLineInFile",
        value = "Insert a line of code at a given line number (0-based) in a file by path"
    )
    public String insertLineInFile(String path, int lineNumber, String lineText)
    throws Exception {
        progress("✏️ Inserting line at " + lineNumber + " in file: " + path);

        return withDocument(path, doc -> {
            try {
                Element root = doc.getDefaultRootElement();
                if (lineNumber < 0 || lineNumber > root.getElementCount()) {
                    progress("⚠️ Invalid line number " + lineNumber + " for file: " + path);
                    return "Invalid line number: " + lineNumber;
                }

                int offset = (lineNumber == root.getElementCount())
                        ? doc.getLength()
                        : root.getElement(lineNumber).getStartOffset();

                doc.insertString(offset, lineText + System.lineSeparator(), null);

                progress("✅ Inserted line at " + lineNumber + " in file: " + path);
                return "Inserted line at " + lineNumber;
            } catch (Exception e) {
                progress("❌ Line insert failed: " + e.getMessage() + " in file: " + path);
                throw e;
            }
        }, true);
    }
}
