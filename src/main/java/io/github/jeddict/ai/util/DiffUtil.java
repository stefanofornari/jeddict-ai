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

import static io.github.jeddict.ai.util.FileUtil.createTempFileObject;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.SingleDiffPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Gaurav Gupta
 */
public class DiffUtil {

    public static void diffAction(boolean classSignature, FileObject fileObject, String signature, JEditorPane editorPane, Map<String, String> cachedMethodSignatures) {
        try {
            JPanel editorParent = (JPanel) editorPane.getParent();
            JPanel diffPanel = new JPanel();
            diffPanel.setLayout(new BorderLayout());
            FileObject source;
            if (classSignature) {
                source = createTempFileObject(fileObject.getName(), cachedMethodSignatures.get(signature));
            } else {
                source = createTempFileObject(fileObject.getName(), fileObject.asText());
                SourceUtil.updateMethod(source, signature, cachedMethodSignatures.get(signature));
            }
            SingleDiffPanel sdp = new SingleDiffPanel(source, fileObject, null);
            diffPanel.add(sdp, BorderLayout.CENTER);

            JButton closeButton = new JButton("Hide Diff View");
            closeButton.setPreferredSize(new Dimension(30, 30));
            closeButton.setContentAreaFilled(false);

            closeButton.addActionListener(e1 -> {
                diffPanel.setVisible(false);
                editorPane.setVisible(true);
                editorParent.revalidate();
                editorParent.repaint();
            });
            diffPanel.add(closeButton, BorderLayout.NORTH);
            int index = editorParent.getComponentZOrder(editorPane);
            editorParent.add(diffPanel, index + 1);
            editorPane.setVisible(false);
            editorParent.revalidate();
            editorParent.repaint();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void diffActionWithSelected(String origin, FileObject fileObject, JEditorPane editorPane) {
        try {
            JPanel editorParent = (JPanel) editorPane.getParent();
            JPanel diffPanel = new JPanel();
            diffPanel.setLayout(new BorderLayout());

            StreamSource ss1 = StreamSource.createSource(
                    "Source",
                    fileObject.getNameExt(),
                    "text/java",
                    new StringReader(origin.trim())
            );
            StreamSource ss2 = StreamSource.createSource(
                    "Target",
                    "AI Generated",
                    "text/java",
                    new StringReader(editorPane.getText())
            );
            DiffView diffView = Diff.getDefault().createDiff(ss2, ss1);
            diffPanel.add(diffView.getComponent(), BorderLayout.CENTER);

            JButton closeButton = new JButton("Hide Diff View");
            closeButton.setPreferredSize(new Dimension(30, 30));
            closeButton.setContentAreaFilled(false);

            closeButton.addActionListener(e1 -> {
                diffPanel.setVisible(false);
                editorPane.setVisible(true);
                editorParent.revalidate();
                editorParent.repaint();
            });
            diffPanel.add(closeButton, BorderLayout.NORTH);
            int index = editorParent.getComponentZOrder(editorPane);
            editorParent.add(diffPanel, index + 1);
            editorPane.setVisible(false);
            editorParent.revalidate();
            editorParent.repaint();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
