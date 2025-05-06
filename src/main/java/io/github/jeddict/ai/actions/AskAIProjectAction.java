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
package io.github.jeddict.ai.actions;

import io.github.jeddict.ai.hints.AssistantChatManager;
import io.github.jeddict.ai.settings.PreferencesManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Edit",
        id = "io.github.jeddict.ai.actions.AskAIProjectAction")
@ActionRegistration(
        displayName = "#CTL_AskAIProjectAction", lazy = false, asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Projects/Actions", position = 100),
})
@Messages({"CTL_AskAIProjectAction=AI Assistant"})
public final class AskAIProjectAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (actionContext != null) {
            return new AskAIProjectAction.ContextAction(
                    PreferencesManager.getInstance().isAiAssistantActivated(),
                    actionContext.lookup(Project.class)
            );
        }
        return new AskAIProjectAction.ContextAction(false, null);
    }

    private static final class ContextAction extends AbstractAction {

        private final Project project;

        private ContextAction(boolean enable, Project project) {
            super(Bundle.CTL_AskAIProjectAction());
            this.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            this.setEnabled(enable);
            this.project = project;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            AssistantChatManager learnFix = new AssistantChatManager(io.github.jeddict.ai.completion.Action.QUERY, project);
            String projectName = ProjectUtils.getInformation(project).getDisplayName();
            learnFix.openChat(null, "", null, projectName, null);
        }

    }
}
