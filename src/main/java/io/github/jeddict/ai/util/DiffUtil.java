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
package io.github.jeddict.ai.util;

import io.github.jeddict.ai.components.diff.DiffView;
import io.github.jeddict.ai.components.diff.FileStreamSource;
import static io.github.jeddict.ai.util.FileUtil.createTempFileObject;
import java.awt.BorderLayout;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import org.netbeans.api.diff.StreamSource;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Gaurav Gupta
 *
 * Example of how to use DiffPanel
 *
    try {
        final String leftString = "Hello\nthis is Stefano\nand not\nSimona";
        final String rightString = "Hello\nthis is Simona\nand not\nStefano";

        final StreamSource leftStream = StreamSource.createSource(
                "Left",
                "Left",
                "text/java",
                new StringReader(leftString)
        );

        final StreamSource rightStream = StreamSource.createSource(
                "Right",
                "Right",
                "text/java",
                new StringReader(rightString)
        );

        FileObject leftFile = createTempFileObject("left", leftString);
        FileObject rightFile = createTempFileObject("right", rightString);

        final FileStreamSource leftEditableStream = new FileStreamSource(leftFile);
        final FileStreamSource rightEditableStream = new FileStreamSource(rightFile);

        //
        // diff between arbitrarily content and a source file
        //
        DiffView diff = new DiffView(leftStream, rightEditableStream);
        TopComponent tc = new TopComponent();
        tc.setName("difftopcomponent");
        tc.setDisplayName("Diff Viewer");
        tc.setLayout(new BorderLayout());
        tc.add(diff, BorderLayout.CENTER);
        tc.open();
        tc.requestActive();
    } catch (IOException x) {
        Exceptions.printStackTrace(x);
    }
 */
public class DiffUtil {

    private static final Logger LOG = Logger.getLogger(DiffUtil.class.getCanonicalName());

    /**
     * This is for the action inside the AI Assistant chat window. It shows the
     * diff between the code (class/method) provided by the assistant (the
     * modified source) and the original file (the base source).
     *
     * @param classSignature
     * @param fileObject
     * @param signature
     * @param editorPane
     * @param cachedMethodSignatures
     */
    public static void diffAction(boolean classSignature, FileObject fileObject, String signature, JEditorPane editorPane, Map<String, String> cachedMethodSignatures) {
        LOG.finest(
            "diff between provided content and " + fileObject +
            " with classSignature " + classSignature +
            " and signature " + signature
        );

        try {
            FileObject source;
            if (classSignature) {
                source = createTempFileObject(fileObject.getName(), cachedMethodSignatures.get(signature));
            } else {
                source = createTempFileObject(fileObject.getName(), fileObject.asText());
                SourceUtil.updateMethod(source, signature, cachedMethodSignatures.get(signature));
            }
            createDiffPanel(editorPane, new FileStreamSource(source), new FileStreamSource(fileObject));
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }
    }

    /**
     * This is for the action inside the AI Assistant chat window. It shows the
     * diff between the code provided by the assistant (the modified source) and
     * the text highlighted in a text component (the base source).
     *
     * @param selectedText
     * @param focusedFile
     * @param editorPane
     */
    public static void diffActionWithSelected(String selectedText, FileObject focusedFile, JEditorPane editorPane) {
        LOG.finest("diff between selected content and " + focusedFile);
        final StreamSource left = StreamSource.createSource(
                "Proposed change",
                "Proposed change",
                focusedFile.getMIMEType(),
                new StringReader(editorPane.getText())
        );
        final StreamSource right = StreamSource.createSource(
                "Original in " + focusedFile.getNameExt(),
                "Original in " + focusedFile.getNameExt(),
                focusedFile.getMIMEType(),
                new StringReader(selectedText.trim())
        );

        createDiffPanel(editorPane, left, right);
    }

    /**
     * This is the action after picking a file in the project component, It
     * shows the diff between the class provided by the assistant (the modified
     * source) and the selected file (the base source)
     *
     * @param modifiedSource
     * @param sourceFile
     * @param editorPane
     */
    public static void diffWithOriginal(
        String modifiedSource, FileObject sourceFile, JEditorPane editorPane
    ) {
        LOG.finest("diff between modified source and base " + ((sourceFile != null) ? sourceFile.getPath() : "null"));
        final StreamSource modified = StreamSource.createSource(
                "Modified " + sourceFile.getNameExt(),
                "Modified " + sourceFile.getNameExt(),
                sourceFile.getMIMEType(),
                new StringReader(modifiedSource)
        );

        createDiffPanel(editorPane, modified, new FileStreamSource(sourceFile));
    }

    private static void createDiffPanel(
        JEditorPane editorPane, StreamSource leftSource, StreamSource rightSource
    ) {
        try {
            final JPanel editorParent = (JPanel) editorPane.getParent();
            final DiffView diffView = (rightSource instanceof FileStreamSource)
                                    ? new DiffView(leftSource, (FileStreamSource)rightSource)
                                    : new DiffView(leftSource, rightSource)
                                    ;

            //
            // When the diffView is closed, it removes itself from its parent
            // component, so we can show againthe nswer from the agent
            //
            editorParent.addContainerListener(new ContainerAdapter() {
                @Override
                public void componentRemoved(ContainerEvent e) {
                    LOG.finest(() -> "removing " + e.getChild() + " from " + editorParent);
                    if (e.getChild() == diffView) {
                        editorPane.setVisible(true);
                    }
                }
            });

            //
            // Add the diffView to the parent just after the text provided by
            // the agent and then hide the text
            //
            int index = editorParent.getComponentZOrder(editorPane);
            editorParent.add(diffView, BorderLayout.CENTER, index + 1);
            editorPane.setVisible(false);
            editorParent.revalidate();
            editorParent.repaint();
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }
    }
}
