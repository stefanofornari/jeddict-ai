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
import org.openide.filesystems.FileObject;

public class GradleUpdateDependencyTool extends BaseBuildTool {

    public GradleUpdateDependencyTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "GradleUpdateDependencyTool_updateDependency",
        value = "Update a dependency in the build.gradle file"
    )
    public String updateDependency(String configuration, String oldDependencyNotation, String newDependencyNotation)
    throws Exception {
        progress("Updating dependency: " + configuration + ":" + oldDependencyNotation + " -> " + newDependencyNotation);
        try {
            FileObject projectDir = projectFileObject();
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                progress("build.gradle not found in project directory");
                return "build.gradle not found in project directory";
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            String oldDependencyString = configuration + " '" + oldDependencyNotation + "'";
            String newDependencyString = configuration + " '" + newDependencyNotation + "'";

            if (!content.contains(oldDependencyString)) {
                progress("Dependency not found: " + oldDependencyString);
                return "Dependency not found in build.gradle";
            }

            String updatedContent = content.replace(oldDependencyString, newDependencyString);

            // Write changes back to build.gradle
            try (OutputStream os = gradleFile.getOutputStream()) {
                os.write(updatedContent.getBytes());
            }

            progress("Dependency updated successfully: " + newDependencyString);
            return "Dependency updated successfully in build.gradle";
        } catch (Exception e) {
            progress("Failed to update dependency: " + e.getMessage());

            throw e;
        }
    }
}
