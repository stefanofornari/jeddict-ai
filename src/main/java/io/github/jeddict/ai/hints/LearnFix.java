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
package io.github.jeddict.ai.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import static com.sun.source.tree.Tree.Kind.CLASS;
import static com.sun.source.tree.Tree.Kind.ENUM;
import static com.sun.source.tree.Tree.Kind.INTERFACE;
import static com.sun.source.tree.Tree.Kind.METHOD;
import com.sun.source.util.TreePath;
import io.github.jeddict.ai.completion.Action;
import io.github.jeddict.ai.JeddictChatModel;
import io.github.jeddict.ai.components.AssistantTopComponent;
import static io.github.jeddict.ai.util.ProjectUtils.getSourceFiles;
import io.github.jeddict.ai.util.StringUtil;
import static io.github.jeddict.ai.util.StringUtil.removeCodeBlockMarkers;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import static io.github.jeddict.ai.util.UIUtil.askQuery;

/**
 *
 * @author Shiwani Gupta
 */
public class LearnFix extends JavaFix {

    private final List<String> acceptedExtensions = Arrays.asList(
            "java", "php", "jsf", "kt", "groovy", "scala", "xml", "json", "yaml", "yml",
            "properties", "txt", "md", "js", "ts", "css", "scss", "html", "xhtml", "sh",
            "bat", "sql", "jsp", "rb", "cs", "go", "swift", "rs", "c", "cpp", "h", "py"
    );

    private TreePath treePath;
    private final Action action;
    private JButton prevButton, nextButton, saveButton;
    private AssistantTopComponent topComponent;
    private final List<String> responseHistory = new ArrayList<>();
    private int currentResponseIndex = -1;
    private String javaCode = null;
    private String projectContent;

    public LearnFix(TreePathHandle tpHandle, Action action, TreePath treePath) {
        super(tpHandle);
        this.treePath = treePath;
        this.action = action;
    }

    public LearnFix(Action action) {
        super(null);
        this.action = action;
    }

    @Override
    protected String getText() {
        if (action == Action.LEARN) {
            return NbBundle.getMessage(JeddictChatModel.class, "HINT_LEARN",
                    StringUtil.convertToCapitalized(treePath.getLeaf().getKind().toString()));
        } else if (action == Action.QUERY) {
            return NbBundle.getMessage(JeddictChatModel.class, "HINT_QUERY",
                    StringUtil.convertToCapitalized(treePath.getLeaf().getKind().toString()));
        }
        return null;
    }

    @Override
    protected void performRewrite(JavaFix.TransformationContext tc) throws Exception {
        WorkingCopy copy = tc.getWorkingCopy();
        if (copy.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
            return;
        }
        Tree leaf = tc.getPath().getLeaf();

        if (leaf.getKind() == CLASS
                || leaf.getKind() == INTERFACE
                || leaf.getKind() == ENUM
                || leaf.getKind() == METHOD) {
            String response;
            if (action == Action.QUERY) {
                String query = askQuery();
                if (query == null) {
                    return;
                }
                response = new JeddictChatModel().generateHtmlDescription(null, treePath.getCompilationUnit().toString(), null, null, query);
            } else {
                if (leaf instanceof MethodTree) {
                    response = new JeddictChatModel().generateHtmlDescriptionForMethod(leaf.toString());
                } else {
                    response = new JeddictChatModel().generateHtmlDescriptionForClass(treePath.getCompilationUnit().toString());

                }
            }
            String name;
            if (leaf instanceof MethodTree) {
                name = ((MethodTree) leaf).getName().toString();
            } else {
                name = ((ClassTree) leaf).getSimpleName().toString();

            }
            displayHtmlContent(removeCodeBlockMarkers(response), name);
        }
    }

    public void askQueryForProject(Project project, String userQuery) {

        ProjectInformation info = ProjectUtils.getInformation(project);
        String projectName = info.getDisplayName();
        List<FileObject> sourceFiles = getSourceFiles(project);

        StringBuilder inputForAI = new StringBuilder();
        for (FileObject file : sourceFiles) {
            try {
                if (acceptedExtensions.contains(file.getExt())) {
                    inputForAI.append(file.asText());
                    inputForAI.append("\n");
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        projectContent = inputForAI.toString();
        String response = new JeddictChatModel().generateHtmlDescriptionForProject(projectContent, userQuery);
        displayHtmlContent(removeCodeBlockMarkers(response), projectName);
    }

    public void askQueryForPackage(Collection<? extends FileObject> selectedPackages, String userQuery) {
        Iterator<? extends FileObject> selectedPackagesIterator = selectedPackages.iterator();
        if (selectedPackagesIterator.hasNext()) {
            Project project = FileOwnerQuery.getOwner(selectedPackagesIterator.next());
            ProjectInformation info = ProjectUtils.getInformation(project);
            String projectName = info.getDisplayName();

            List<FileObject> sourceFiles = selectedPackages.stream()
                    .filter(FileObject::isFolder)
                    .flatMap(packageFolder -> Arrays.stream(packageFolder.getChildren())
                    .filter(FileObject::isData)
                    .filter(file -> acceptedExtensions.contains(file.getExt())))
                    .collect(Collectors.toList());

            StringBuilder inputForAI = new StringBuilder();
            for (FileObject file : sourceFiles) {
                try {
                    inputForAI.append(file.asText());
                    inputForAI.append("\n");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            projectContent = inputForAI.toString();
            String response = new JeddictChatModel().generateHtmlDescriptionForProject(projectContent, userQuery);
            displayHtmlContent(removeCodeBlockMarkers(response), projectName);
        }
    }

    private void displayHtmlContent(final String response, String title) {
        SwingUtilities.invokeLater(() -> {
            try {
                File tempFile = File.createTempFile("tempHtml", ".html");
                tempFile.deleteOnExit();
                try (FileWriter writer = new FileWriter(tempFile)) {
                    writer.write(response);
                }

                Preferences prefs = Preferences.userNodeForPackage(AssistantTopComponent.class);
                prefs.putBoolean(AssistantTopComponent.PREFERENCE_KEY, true);
                topComponent = new AssistantTopComponent(title + " AI Assistance");
                updateEditor(response).addHyperlinkListener((HyperlinkEvent e) -> {
                    if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (IOException | URISyntaxException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                JScrollPane scrollPane = new JScrollPane(topComponent.getParentPanel());
                topComponent.add(scrollPane, BorderLayout.CENTER);

                responseHistory.add(response);
                currentResponseIndex = responseHistory.size() - 1;

                topComponent.add(createBottomPanel(title), BorderLayout.SOUTH);
                topComponent.open();
                topComponent.requestActive();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (topComponent != null) {
                        topComponent.close();
                    }
                }));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private JPanel createBottomPanel(String title) {
        // Create a panel for the text field and buttons
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);

        // Previous Button
        prevButton = new JButton("\u2190"); // Left arrow (←)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        bottomPanel.add(prevButton, gbc);

        // Next Button
        nextButton = new JButton("\u2192");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        bottomPanel.add(nextButton, gbc);

        // Text Field
        JTextField questionField = new JTextField();
        questionField.setPreferredSize(new Dimension(300, questionField.getPreferredSize().height));
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        bottomPanel.add(questionField, gbc);

        // Ask Button
        JButton submitButton = new JButton("Ask");
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        bottomPanel.add(submitButton, gbc);

        // Save Button
        saveButton = new JButton("Copy");
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        bottomPanel.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            try {
                StringSelection selection = new StringSelection(javaCode);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JButton upButton = new JButton("\u2191"); // Up arrow (↑)
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; // No expansion
        bottomPanel.add(upButton, gbc);

        upButton.addActionListener(e -> {
            try {
                File latestTempFile = File.createTempFile(title, ".html");
                latestTempFile.deleteOnExit();
                try (FileWriter writer = new FileWriter(latestTempFile)) {
                    writer.write(responseHistory.get(currentResponseIndex));
                }
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(latestTempFile.toURI());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Create a common action listener method
        ActionListener submitActionListener = e -> {
            String question = questionField.getText();
            if (!question.isEmpty()) {
                submitButton.setText("Loading...");
                submitButton.setEnabled(false);
                handleQuestion(question, submitButton);
            }
        };

        submitButton.addActionListener(submitActionListener);
        questionField.addActionListener(submitActionListener);

        prevButton.addActionListener(e -> {
            if (currentResponseIndex > 0) {
                currentResponseIndex--;
                String historyResponse = responseHistory.get(currentResponseIndex);
                updateEditor(historyResponse);
                updateNavigationButtons(prevButton, nextButton);
            }
        });
        nextButton.addActionListener(e -> {
            if (currentResponseIndex < responseHistory.size() - 1) {
                currentResponseIndex++;
                String historyResponse = responseHistory.get(currentResponseIndex);
                updateEditor(historyResponse);
                updateNavigationButtons(prevButton, nextButton);
            }
        });

        updateNavigationButtons(prevButton, nextButton);

        return bottomPanel;
    }

    private JEditorPane updateEditor(String response) {
        JEditorPane editorPane = null;
        String[] parts = response.split("<pre><code type=\"[^\"]*\">|<pre><code class=\"[^\"]*\">|<pre><code class=\"[^\"]*\" type=\"[^\"]*\">|<pre><code type=\"[^\"]*\" class=\"[^\"]*\">|</code></pre>");
        topComponent.clear();
        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 1) {
                editorPane = topComponent.createCodePane(parts[i]);
                javaCode = parts[i];
            } else {
                editorPane = topComponent.createHtmlPane(parts[i]);
            }
        }
        if (editorPane != null) {
            editorPane.setCaretPosition(editorPane.getDocument().getLength());
        }
        return editorPane;
    }

    private void handleQuestion(String question, JButton submitButton) {
        SwingUtilities.invokeLater(() -> {
            try {
                String prevChat = responseHistory.get(responseHistory.size() - 1);
                if (action == Action.LEARN) {
                    if (responseHistory.size() - 1 == 0) {
                        prevChat = null;
                    }
                }
                String response;
                if (treePath == null && projectContent != null) {
                    response = new JeddictChatModel().generateHtmlDescription(projectContent, null, null, prevChat, question);
                } else {
                    response = new JeddictChatModel().generateHtmlDescription(null, treePath.getCompilationUnit().toString(), treePath.getLeaf() instanceof MethodTree ? treePath.getLeaf().toString() : null, prevChat, question);
                }
                response = removeCodeBlockMarkers(response);
                if (responseHistory.isEmpty() || !response.equals(responseHistory.get(responseHistory.size() - 1))) {
                    responseHistory.add(response);
                    currentResponseIndex = responseHistory.size() - 1;
                }
                updateEditor(response);
                submitButton.setText("Ask");
                submitButton.setEnabled(true);
                saveButton.setEnabled(javaCode != null);
                updateNavigationButtons(prevButton, nextButton);
            } catch (Exception e) {
                e.printStackTrace();
                submitButton.setText("Ask");
                submitButton.setEnabled(true);
            }
        });
    }

    private void updateNavigationButtons(JButton prevButton, JButton nextButton) {
        prevButton.setEnabled(currentResponseIndex > 0);
        nextButton.setEnabled(currentResponseIndex < responseHistory.size() - 1);
    }

}
