/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.jeddict.ai.actions;

import io.github.jeddict.ai.hints.LearnFix;
import io.github.jeddict.ai.settings.PreferencesManager;
import io.github.jeddict.ai.util.UIUtil;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Shiwani Gupta
 */
@ActionID(
        category = "Project",
        id = "io.github.jeddict.ai.actions.GenerateCommitMessageAction")
@ActionRegistration(
        displayName = "#CTL_GenerateCommitMessageAction", lazy = false, asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Projects/Actions", position = 100),})
@Messages({"CTL_GenerateCommitMessageAction=AI Commit Message"})
public final class GenerateCommitMessageAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent ev) {
        // This can remain empty as the context action will handle the logic
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (actionContext != null) {
            Project project = actionContext.lookup(Project.class);
            boolean isGitProject = project != null && isGitRepository(project);
            return new GenerateCommitMessageAction.ContextAction(
                    isGitProject && PreferencesManager.getInstance().isAiAssistantActivated(),
                    project
            );
        }
        return new GenerateCommitMessageAction.ContextAction(false, null);
    }

    private boolean isGitRepository(Project project) {
        File projectDir = FileUtil.toFile(project.getProjectDirectory()); // Get the project directory as a File
        if (projectDir != null) {
            File gitDir = new File(projectDir, ".git"); // Check for .git directory
            return gitDir.exists() && gitDir.isDirectory(); // Ensure it exists and is a directory
        }
        return false;
    }

    private static final class ContextAction extends AbstractAction {

        private final Project project;

        private ContextAction(boolean enable, Project project) {
            super(Bundle.CTL_GenerateCommitMessageAction());
            this.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            this.setEnabled(enable);
            this.project = project;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            StringBuilder sb = new StringBuilder();
            sb.append("git diff \n\n");
            String diffOutput = runGitCommand("diff");
            sb.append(diffOutput);
            sb.append("\n\n git status \n\n");
            String statusOutput = runGitCommand("status");
            sb.append(statusOutput);
            String intitalCommitMessage = UIUtil.askForInitialCommitMessage();
            LearnFix learnFix = new LearnFix(io.github.jeddict.ai.completion.Action.QUERY);
            learnFix.askQueryForProjectCommit(project, sb.toString(), intitalCommitMessage);
        }

        private String runGitCommand(String command) {
            StringBuilder output = new StringBuilder();
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("git", command);
                File projectDir = FileUtil.toFile(project.getProjectDirectory()); // Convert FileObject to File
                processBuilder.directory(projectDir); // Set the working directory to the project root
                Process process = processBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace(); // Handle exceptions appropriately in your production code
            }
            return output.toString();
        }

    }
}
