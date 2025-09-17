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
package io.github.jeddict.ai.agent.tools.project;

import dev.langchain4j.agent.tool.Tool;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class GraddleListDependenciesTool extends BaseBuildTool {

    public GraddleListDependenciesTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "GraddleListDependenciesTool_listDependencies",
        value = "List all dependencies in the build.gradle file"
    )
    public String listDependencies() throws Exception {
        progress("Listing dependencies");
        try {
            FileObject projectDir = FileUtil.toFileObject(Paths.get(basedir));
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                progress("build.gradle not found in project directory");
                return "build.gradle not found in project directory";
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            // Extract dependencies block
            int dependenciesIndex = content.indexOf("dependencies {");
            if (dependenciesIndex == -1) {
                progress("No dependencies section found in build.gradle");
                return "No dependencies section found in build.gradle";
            }
            int endIndex = content.indexOf("}", dependenciesIndex);
            if (endIndex == -1) {
                progress("Malformed dependencies section in build.gradle");
                return "Malformed dependencies section in build.gradle";
            }

            String dependenciesBlock = content.substring(dependenciesIndex, endIndex + 1);
            String[] lines = dependenciesBlock.split("\n");
            List<String> dependencies = new ArrayList<>();
            for (String line : lines) {
                line = line.trim();
                if (!line.startsWith("dependencies") && !line.equals("{") && !line.equals("}") && !line.isEmpty()) {
                    dependencies.add(line);
                }
            }

            progress("Dependencies listed");
            if (dependencies.isEmpty()) {
                return "No dependencies found in build.gradle";
            }
            return String.join("\n", dependencies);
        } catch (Exception e) {
            progress("Failed to list dependencies: " + e.getMessage());

            throw e;
        }
    }
}
