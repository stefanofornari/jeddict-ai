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
package io.github.jeddict.ai.agent;

import dev.langchain4j.agent.tool.Tool;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import io.github.jeddict.ai.lang.JeddictStreamHandler;
import java.io.OutputStream;

/**
 * Tool to manage dependencies in the build.gradle file.
 * Allows adding, removing, updating, and listing dependencies in the project's build.gradle.
 *
 * Author: Assistant
 */
public class GradleTools {

    private final Project project;
    private final JeddictStreamHandler handler;
    private final static Logger logger = Logger.getLogger(GradleTools.class.getName());

    public GradleTools(Project project, JeddictStreamHandler handler) {
        this.project = project;
        this.handler = handler;
    }

    @Tool("Add a dependency to the build.gradle file")
    public String addDependency(String configuration, String dependencyNotation) {
        log("Adding dependency", configuration + ":" + dependencyNotation);
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                log("build.gradle not found in project directory", null);
                return "build.gradle not found in project directory";
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            String dependencyString = configuration + " '" + dependencyNotation + "'";

            if (content.contains(dependencyString)) {
                log("Dependency already exists", dependencyString);
                return "Dependency already exists in build.gradle";
            }

            // Find dependencies block and add the dependency inside it
            String updatedContent;
            int dependenciesIndex = content.indexOf("dependencies {");
            if (dependenciesIndex == -1) {
                // No dependencies block, add one at the end
                updatedContent = content + "\n\ndependencies {\n    " + dependencyString + "\n}\n";
            } else {
                int insertIndex = content.indexOf("}", dependenciesIndex);
                if (insertIndex == -1) {
                    // Malformed file? Just append
                    updatedContent = content + "\n    " + dependencyString + "\n";
                } else {
                    updatedContent = content.substring(0, insertIndex) + "    " + dependencyString + "\n" + content.substring(insertIndex);
                }
            }

            // Write changes back to build.gradle
            try (OutputStream os = gradleFile.getOutputStream()) {
                os.write(updatedContent.getBytes());
            }

            log("Dependency added successfully", dependencyString);
            return "Dependency added successfully to build.gradle";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error adding dependency to build.gradle", ex);
            log("Failed to add dependency", ex.getMessage());
            return "Failed to add dependency: " + ex.getMessage();
        }
    }

    @Tool("Remove a dependency from the build.gradle file")
    public String removeDependency(String configuration, String dependencyNotation) {
        log("Removing dependency", configuration + ":" + dependencyNotation);
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                log("build.gradle not found in project directory", null);
                return "build.gradle not found in project directory";
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            String dependencyString = configuration + " '" + dependencyNotation + "'";

            if (!content.contains(dependencyString)) {
                log("Dependency not found", dependencyString);
                return "Dependency not found in build.gradle";
            }

            // Remove the dependency string
            String updatedContent = content.replace(dependencyString + "\n", "");
            updatedContent = updatedContent.replace(dependencyString, "");

            // Write changes back to build.gradle
            try (OutputStream os = gradleFile.getOutputStream()) {
                os.write(updatedContent.getBytes());
            }

            log("Dependency removed successfully", dependencyString);
            return "Dependency removed successfully from build.gradle";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error removing dependency from build.gradle", ex);
            log("Failed to remove dependency", ex.getMessage());
            return "Failed to remove dependency: " + ex.getMessage();
        }
    }

    @Tool("List all dependencies in the build.gradle file")
    public String listDependencies() {
        log("Listing dependencies", null);
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                log("build.gradle not found in project directory", null);
                return "build.gradle not found in project directory";
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            // Extract dependencies block
            int dependenciesIndex = content.indexOf("dependencies {");
            if (dependenciesIndex == -1) {
                log("No dependencies section found in build.gradle", null);
                return "No dependencies section found in build.gradle";
            }
            int endIndex = content.indexOf("}", dependenciesIndex);
            if (endIndex == -1) {
                log("Malformed dependencies section in build.gradle", null);
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

            log("Dependencies listed", null);
            if (dependencies.isEmpty()) {
                return "No dependencies found in build.gradle";
            }
            return String.join("\n", dependencies);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error listing dependencies from build.gradle", ex);
            log("Failed to list dependencies", ex.getMessage());
            return "Failed to list dependencies: " + ex.getMessage();
        }
    }

    @Tool("Update a dependency in the build.gradle file")
    public String updateDependency(String configuration, String oldDependencyNotation, String newDependencyNotation) {
        log("Updating dependency", configuration + ":" + oldDependencyNotation + " -> " + newDependencyNotation);
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                log("build.gradle not found in project directory", null);
                return "build.gradle not found in project directory";
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            String oldDependencyString = configuration + " '" + oldDependencyNotation + "'";
            String newDependencyString = configuration + " '" + newDependencyNotation + "'";

            if (!content.contains(oldDependencyString)) {
                log("Dependency not found", oldDependencyString);
                return "Dependency not found in build.gradle";
            }

            String updatedContent = content.replace(oldDependencyString, newDependencyString);

            // Write changes back to build.gradle
            try (OutputStream os = gradleFile.getOutputStream()) {
                os.write(updatedContent.getBytes());
            }

            log("Dependency updated successfully", newDependencyString);
            return "Dependency updated successfully in build.gradle";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error updating dependency in build.gradle", ex);
            log("Failed to update dependency", ex.getMessage());
            return "Failed to update dependency: " + ex.getMessage();
        }
    }

    @Tool("Check if a dependency exists in the build.gradle file")
    public boolean dependencyExists(String configuration, String dependencyNotation) {
        log("Checking dependency existence", configuration + ":" + dependencyNotation);
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                log("build.gradle not found in project directory", null);
                return false;
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            String dependencyString = configuration + " '" + dependencyNotation + "'";

            boolean exists = content.contains(dependencyString);
            log(exists ? "Dependency exists" : "Dependency does not exist", dependencyString);
            return exists;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error checking dependency existence in build.gradle", ex);
            log("Failed to check dependency existence", ex.getMessage());
            return false;
        }
    }

    private void log(String action, String detail) {
        if (handler != null) {
            String message = action + (detail != null ? (": " + detail) : "") + "\n";
            handler.onToolingResponse(message);
        }
    }

}
