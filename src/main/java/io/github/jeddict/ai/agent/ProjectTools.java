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
import io.github.jeddict.ai.lang.JeddictStreamHandler;
import io.github.jeddict.ai.settings.PreferencesManager;
import io.github.jeddict.ai.util.FileUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import org.netbeans.api.project.Project;

/**
 *
 * @author Gaurav Gupta
 */
public class ProjectTools {

    private final Project project;
    private final JeddictStreamHandler handler;
    private final String buildCommand, testCommand;
    protected static PreferencesManager pm = PreferencesManager.getInstance();

    public ProjectTools(Project project, JeddictStreamHandler handler) {
        this.project = project;
        this.handler = handler;
        this.buildCommand = pm.getBuildCommand(project);
        this.testCommand = pm.getTestCommand(project);
    }

    @Tool("Build the project and return full log")
    public String buildProject() {
        return runCommand(buildCommand, "Building");
    }

    @Tool("Run project tests and return full log")
    public String testProject() {
        return runCommand(testCommand, "Testing");
    }

    /**
     * Runs a command in the project directory, streams output, and returns the
     * full log.
     *
     * @param goals the Maven goals to run (e.g., "clean install" or "test")
     * @param actionLabel the label to show in the AI stream ("Building",
     * "Testing", etc.)
     * @return full log of the Maven command
     */
    private String runCommand(String goals, String actionLabel) {
        log(actionLabel, project.getProjectDirectory().getNameExt());
        StringBuilder fullLog = new StringBuilder();

        try {
            Path projectDir = FileUtil.resolvePath(project, ".");
            ProcessBuilder pb;

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Run via cmd /c on Windows
                pb = new ProcessBuilder("cmd", "/c", goals)
                        .directory(projectDir.toFile())
                        .redirectErrorStream(true);
            } else {
                // Unix/Mac
                pb = new ProcessBuilder("sh", "-c", goals)
                        .directory(projectDir.toFile())
                        .redirectErrorStream(true);
            }

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fullLog.append(line).append("\n");
                    log(line); // stream line to AI agent
                }
            }

            int exitCode = process.waitFor();
            String result = (exitCode == 0 ? actionLabel + " successful"
                    : actionLabel + " failed with exit code " + exitCode);
            log(actionLabel + " finished", result);
            fullLog.append(result).append("\n");

        } catch (Exception e) {
            String error = actionLabel + " failed: " + e.getMessage();
            log(actionLabel + " error", error);
            fullLog.append(error).append("\n");
        }

        return fullLog.toString();
    }

    /**
     * Logs the current action to the {@link JeddictStreamHandler}, if
     * available.
     *
     * @param action the action being performed (e.g., "Building", "Testing")
     * @param message the message related to the action is performed
     */
    private void log(String action, String message) {
        if (handler != null && message != null) {
            handler.onToolingResponse(action + " " + message + "\n");
        }
    }

    private void log(String message) {
        if (handler != null) {
            handler.onToolingResponse(message + "\n");
        }
    }

}
