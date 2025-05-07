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

import io.github.jeddict.ai.completion.SQLCompletion;
import io.github.jeddict.ai.hints.AssistantChatManager;
import io.github.jeddict.ai.settings.PreferencesManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "File",
        id = "io.github.jeddict.ai.actions.AskAIDBAction")
@ActionRegistration(
        displayName = "#CTL_AskAIDBAction", lazy = false, asynchronous = true)
@ActionReferences({
 @ActionReference(path = "Databases/Explorer/Connection/Actions", position = 350),
//    @ActionReference(path = "Databases/Explorer/Catalog/Actions", position = 350),
//    @ActionReference(path = "Databases/Explorer/Schema/Actions", position = 350),
//    @ActionReference(path = "Databases/Explorer/TableList/Actions", position = 350),
//    @ActionReference(path = "Databases/Explorer/SystemTableList/Actions", position = 350),
//    @ActionReference(path = "Databases/Explorer/Table/Actions", position = 250),
//    @ActionReference(path = "Databases/Explorer/ViewList/Actions", position = 350),
})

@Messages({"CTL_AskAIDBAction=AI Assistant"})
public final class AskAIDBAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (actionContext != null) {
            return new AskAIDBAction.ContextAction(
                    PreferencesManager.getInstance().isAiAssistantActivated(),
                    actionContext.lookup(DatabaseConnection.class)
            );
        }
        return new AskAIDBAction.ContextAction(false, null);
    }

    private static final class ContextAction extends AbstractAction {

        private final DatabaseConnection connection;

        private ContextAction(boolean enable, DatabaseConnection connection) {
            super(Bundle.CTL_AskAIDBAction());
            this.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            this.setEnabled(enable);
            this.connection = connection;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (connection == null) {
               JOptionPane.showMessageDialog(null, "Warning: Database connection is not active!", "Connection Error", javax.swing.JOptionPane.WARNING_MESSAGE);
               return;
            }
            SQLCompletion sqlCompletion = new SQLCompletion(connection);
            AssistantChatManager learnFix = new AssistantChatManager(io.github.jeddict.ai.completion.Action.QUERY, sqlCompletion);
            learnFix.openChat("sql", "", null, "AI Chat ["+connection.getDisplayName()+']', null);
        }

    }
}