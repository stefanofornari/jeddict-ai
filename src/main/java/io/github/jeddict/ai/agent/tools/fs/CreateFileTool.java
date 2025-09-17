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
package io.github.jeddict.ai.agent.tools.fs;

import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateFileTool extends BaseFileSystemTool {

    public CreateFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "CreateFileTool_createFile",
        value = "Create a new file at the given path with optional content"
    )
    public String createFile(String path, String content) throws Exception {
        progress("📄 Creating new file: " + path);
        try {
            Path filePath = Paths.get(basedir, path);
            if (Files.exists(filePath)) {
                progress("⚠️ File already exists: " + path);
                return "File already exists: " + path;
            }

            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content != null ? content : "");

            progress("✅ File created successfully: " + path);
            return "File created";
        } catch (IOException e) {
            progress("❌ File creation failed: " + e.getMessage() + " in file: " + path);
            throw e;
        }
    }
}
