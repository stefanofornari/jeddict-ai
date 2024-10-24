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
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Project",
        id = "io.github.jeddict.ai.actions.AskAIPackageAction")
@ActionRegistration(
        displayName = "#CTL_AskAIPackageAction", lazy = true, asynchronous = true, iconBase = "icons/logo28.png")
@ActionReferences({
    @ActionReference(path = "Projects/package/Actions", position = 100),
    @ActionReference(path = "UI/ToolActions/Files", position = 2045),
    @ActionReference(path = "Toolbars/Build", position = 100)})
@Messages({"CTL_AskAIPackageAction=AI Assistant"})
public final class AskAIPackageAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent ev) {
        LearnFix learnFix = new LearnFix(io.github.jeddict.ai.completion.Action.QUERY);
        learnFix.openChat(null, "", null, "AI Assistant", null);
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (actionContext != null) {
            return new AskAIPackageAction.ContextAction(
                    PreferencesManager.getInstance().isAiAssistantActivated(),
                    actionContext.lookupAll(FileObject.class)
            );
        }
        return new AskAIPackageAction.ContextAction(false, null);
    }

    private static final class ContextAction extends AbstractAction {

        private final Collection<? extends FileObject> selectedFileObjects;

        private ContextAction(boolean enable, Collection<? extends FileObject> selectedPackages) {
            super(Bundle.CTL_AskAIPackageAction());
            this.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            this.setEnabled(enable);
            this.selectedFileObjects = selectedPackages;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            LearnFix learnFix = new LearnFix(io.github.jeddict.ai.completion.Action.QUERY, selectedFileObjects);
            Iterator<? extends FileObject> selectedPackagesIterator = selectedFileObjects.iterator();
            if (selectedPackagesIterator.hasNext()) {
                Project project = FileOwnerQuery.getOwner(selectedPackagesIterator.next());
                String projectName = ProjectUtils.getInformation(project).getDisplayName();
                learnFix.openChat(null, "", null, projectName + "* AI Assistant", null);
            }
        }

    }
}
