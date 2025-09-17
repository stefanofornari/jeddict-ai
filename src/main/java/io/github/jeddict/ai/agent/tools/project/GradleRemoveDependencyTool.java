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
import java.io.OutputStream;
import java.nio.file.Paths;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class GradleRemoveDependencyTool extends BaseBuildTool {

    public GradleRemoveDependencyTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "GradleRemoveDependencyTool_removeDependency",
        value = "Remove a dependency from the build.gradle file"
    )
    public String removeDependency(String configuration, String dependencyNotation)
    throws Exception {
        progress("Removing dependency: " + configuration + ":" + dependencyNotation);
        try {
            FileObject projectDir = FileUtil.toFileObject(Paths.get(basedir));
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                progress("build.gradle not found in project directory");
                return "build.gradle not found in project directory";
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            String dependencyString = configuration + " '" + dependencyNotation + "'";

            if (!content.contains(dependencyString)) {
                progress("Dependency not found: " + dependencyString);
                return "Dependency not found in build.gradle";
            }

            // Remove the dependency string
            String updatedContent = content.replace(dependencyString + "\n", "");
            updatedContent = updatedContent.replace(dependencyString, "");

            // Write changes back to build.gradle
            try (OutputStream os = gradleFile.getOutputStream()) {
                os.write(updatedContent.getBytes());
            }

            progress("Dependency removed successfully: " + dependencyString);
            return "Dependency removed successfully from build.gradle";
        } catch (Exception e) {
            progress("Failed to remove dependency: " + e.getMessage());

            throw e;
        }
    }
}
