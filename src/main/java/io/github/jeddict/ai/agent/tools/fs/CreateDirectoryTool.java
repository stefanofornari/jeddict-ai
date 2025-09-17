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

public class CreateDirectoryTool extends BaseFileSystemTool {

    public CreateDirectoryTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "CreateDirectoryTool_createDirectory",
        value = "Create a new directory at the given path"
    )
    public String createDirectory(String path) throws Exception {
        progress("📂 Creating new directory: " + path);
        try {
            Path dirPath = fullPath(path);
            if (Files.exists(dirPath)) {
                progress("⚠️ Directory already exists: " + path);
                return "Directory already exists: " + path;
            }

            Files.createDirectories(dirPath);
            progress("✅ Directory created successfully: " + path);
            return "Directory created";
        } catch (IOException e) {
            progress("❌ Directory creation failed: " + e.getMessage() + " in " + path);
            throw e;
        }
    }
}
