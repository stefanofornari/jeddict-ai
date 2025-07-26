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

import io.github.jeddict.ai.components.AssistantChat;
import io.github.jeddict.ai.hints.AssistantChatManager;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.*;
import org.openide.filesystems.FileObject;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Project",
        id = "io.github.jeddict.ai.actions.AddToChatContextAction"
)
@ActionRegistration(
        displayName = "#CTL_AddToChatContextAction",
        lazy = false,
        asynchronous = false
)
@ActionReferences({
    @ActionReference(path = "Projects/package/Actions", position = 101),
    @ActionReference(path = "Loaders/text/x-java/Actions", position = 101),
    @ActionReference(path = "Loaders/folder/any/Actions", position = 301)
})
@Messages({"CTL_AddToChatContextAction=Add to Chat Session Context"})
public final class AddToChatContextAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        // nikdy nebude volaná priamo - len cez ContextAction
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        List<FileObject> files = new ArrayList<>(context.lookupAll(FileObject.class));
        boolean en = !files.isEmpty() && isAssistantChatOpened();
        return new ContextAction(en, files);
    }

    @Override
    public boolean isEnabled() {
        return isAssistantChatOpened();
    }

    private static boolean isAssistantChatOpened() {
        Set<? extends Mode> modes = WindowManager.getDefault().getModes();
        for (Mode mode : modes) {
            TopComponent[] openedTopComponents = WindowManager.getDefault().getOpenedTopComponents(mode);
            for (TopComponent tc : openedTopComponents) {
                if (tc instanceof AssistantChat) {
                    if (tc.isOpened() && tc.isShowing()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static TopComponent getOpenedAssistantChat() {
        Set<? extends Mode> modes = WindowManager.getDefault().getModes();
        for (Mode mode : modes) {
            TopComponent[] openedTopComponents = WindowManager.getDefault().getOpenedTopComponents(mode);
            for (TopComponent tc : openedTopComponents) {
                if (tc instanceof AssistantChat) {
                    if (tc.isOpened() && tc.isShowing()) {
                        return tc;
                    }
                }
            }
        }
        return null;
    }

    private static final class ContextAction extends AbstractAction {

        private final List<FileObject> files;

        private ContextAction(boolean enabled, List<FileObject> files) {
            super(Bundle.CTL_AddToChatContextAction());
            this.files = files;
            this.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            setEnabled(enabled);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TopComponent openedAssistantChat = getOpenedAssistantChat();
            if (openedAssistantChat != null) {
                Object clientProperty = openedAssistantChat.getClientProperty(AssistantChatManager.ASSISTANT_CHAT_MANAGER_KEY);
                if (clientProperty instanceof WeakReference) {
                    Object tmp = ((WeakReference) clientProperty).get();
                    if (tmp instanceof AssistantChatManager) {
                        ((AssistantChatManager) tmp).addToSessionContext(files);
                    }
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return isAssistantChatOpened();
        }

    }
}
