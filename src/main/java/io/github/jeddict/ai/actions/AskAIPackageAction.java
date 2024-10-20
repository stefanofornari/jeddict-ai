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
import static io.github.jeddict.ai.util.UIUtil.askQuery;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
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
        displayName = "#CTL_AskAIPackageAction", lazy = false, asynchronous = true)
@ActionReference(path = "Projects/package/Actions", position = 100)
@Messages({"CTL_AskAIPackageAction=AI Query Package"})
public final class AskAIPackageAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent ev) {
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

        private final Collection<? extends FileObject> selectedPackages;

        private ContextAction(boolean enable, Collection<? extends FileObject> selectedPackages) {
            super(Bundle.CTL_AskAIPackageAction());
            this.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            this.setEnabled(enable);
            this.selectedPackages = selectedPackages;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String query = askQuery();
            if (query == null) {
                return;
            }
            LearnFix learnFix = new LearnFix(io.github.jeddict.ai.completion.Action.QUERY);
            learnFix.askQueryForPackage(selectedPackages, query);
        }

    }
}
