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
import org.openide.filesystems.FileObject;

public class GradleDependencyExistsTool extends BaseBuildTool {

    public GradleDependencyExistsTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "GradleDependencyExistsTool_dependencyExists",
        value = "Check if a dependency exists in the build.gradle file"
    )
    public boolean dependencyExists(String configuration, String dependencyNotation)
    throws Exception {
        progress("Checking dependency existence: " + configuration + ":" + dependencyNotation);
        try {
            FileObject projectDir = projectFileObject();
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                progress("build.gradle not found in project directory");
                return false;
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            String dependencyString = configuration + " '" + dependencyNotation + "'";

            boolean exists = content.contains(dependencyString);
            progress((exists ? "Dependency exists" : "Dependency does not exist") + ": " + dependencyString);
            return exists;
        } catch (Exception e) {
            progress("Failed to check dependency existence: " + e.getMessage());

            throw e;
        }
    }
}
