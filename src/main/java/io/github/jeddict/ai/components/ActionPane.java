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
package io.github.jeddict.ai.components;

import io.github.jeddict.ai.agent.FileAction;
import static io.github.jeddict.ai.components.AssistantChat.createEditorKit;
import io.github.jeddict.ai.components.diff.DiffView;
import io.github.jeddict.ai.components.diff.FileStreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.text.EditorKit;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 */
public class ActionPane extends JTabbedPane {

    private static final Logger LOG = Logger.getLogger(ActionPane.class.getCanonicalName());

    private final FileAction action;
    private final Project project;

    public ActionPane(final Project project, final FileAction action) {
        this.project = project;
        this.action = action;
    }

    public JEditorPane createPane() {
        //
        // show two tabs, one with the provided source and one with the diff
        //
        //
        // TODO: refactor and use DiffUtil?
        // TODO: handle teh case fo is null, which means the file path provided
        //       in the action by the AI is not valid (any more)
        //
        FileObject fo = FileUtil.toFileObject(
            FileUtil.normalizeFile(new File(project.getProjectDirectory().getPath(), action.path()))
        );

        try {
            DiffView diffView = new DiffView(
                StreamSource.createSource(
                    "Modified " + action.path(),
                    "Modified " + action.path(),
                    fo.getMIMEType(),
                    new StringReader(action.content())
                ),
                new FileStreamSource(fo)
            );
            addTab("Diff", diffView);
        } catch (IOException x) {
            final String msg = "error creating a diff view for action " + action;
            LOG.severe(msg); Exceptions.printStackTrace(x);

            return new JEditorPane("plain/text", msg);
        }

        JEditorPane editorPane = new JEditorPane();
        EditorKit editorKit = createEditorKit(fo.getMIMEType());
        editorPane.setEditorKit(editorKit);
        editorPane.setText(action.content());
        editorPane.setEditable(false);
        addTab("Source", editorPane);

        return editorPane;
    }
}
