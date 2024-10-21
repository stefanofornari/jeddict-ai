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
import static io.github.jeddict.ai.components.AssistantTopComponent.backIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.copyIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.createEditorKit;
import static io.github.jeddict.ai.components.AssistantTopComponent.forwardIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.newEditor;
import static io.github.jeddict.ai.components.AssistantTopComponent.progressIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.saveToEditor;
import static io.github.jeddict.ai.components.AssistantTopComponent.saveasIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.startIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.upIcon;
import io.github.jeddict.ai.settings.PreferencesManager;
import static io.github.jeddict.ai.util.ProjectUtils.getSourceFiles;
import static io.github.jeddict.ai.util.SourceUtil.removeJavadoc;
import io.github.jeddict.ai.util.StringUtil;
import static io.github.jeddict.ai.util.StringUtil.removeCodeBlockMarkers;
import java.awt.BorderLayout;
import java.awt.Desktop;
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;

/**
 *
 * @author Shiwani Gupta
 */
public class LearnFix extends JavaFix {

    private TreePath treePath;
    private final Action action;
    private JButton prevButton, nextButton, copyButton, saveButton, openInBrowserButton;
    private AssistantTopComponent topComponent;
    private final List<String> responseHistory = new ArrayList<>();
    private int currentResponseIndex = -1;
    private String javaCode = null;
    private Project project;
    private String projectContent;
    private String commitChanges;
    private PreferencesManager pm = PreferencesManager.getInstance();
    private Tree leaf;

    public LearnFix(TreePathHandle tpHandle, Action action, TreePath treePath) {
        super(tpHandle);
        this.treePath = treePath;
        this.action = action;
    }

    public LearnFix(Action action) {
        super(null);
        this.action = action;
    }

    public LearnFix(Action action, Project project) {
        super(null);
        this.action = action;
        this.project = project;
    }

    @Override
    protected String getText() {
        if (action == Action.LEARN) {
            return NbBundle.getMessage(JeddictChatModel.class, "HINT_LEARN",
                    StringUtil.convertToCapitalized(treePath.getLeaf().getKind().toString()));
        } else if (action == Action.QUERY) {
            return NbBundle.getMessage(JeddictChatModel.class, "HINT_QUERY",
                    StringUtil.convertToCapitalized(treePath.getLeaf().getKind().toString()));
        } else if (action == Action.TEST) {
            return NbBundle.getMessage(JeddictChatModel.class, "HINT_TEST",
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
        leaf = tc.getPath().getLeaf();
        String fileName = null;
        FileObject fileObject = copy.getFileObject();
        if (fileObject != null) {
            fileName = fileObject.getName();
            this.project = FileOwnerQuery.getOwner(fileObject);
        }

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
            } else if (action == Action.TEST) {
                if (leaf instanceof MethodTree) {
                    response = new JeddictChatModel().generateTestCase(null, null, leaf.toString(), null, null);
                } else {
                    response = new JeddictChatModel().generateTestCase(null, treePath.getCompilationUnit().toString(), null, null, null);
                }
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
            displayHtmlContent(removeCodeBlockMarkers(response), fileName, name + " AI Assistance");
        }
    }

    public void askQueryForProject(Project project, String userQuery) {

        ProjectInformation info = ProjectUtils.getInformation(project);
        String projectName = info.getDisplayName();
        List<FileObject> sourceFiles = getSourceFiles(project);

        StringBuilder inputForAI = new StringBuilder();
        for (FileObject file : sourceFiles) {
            try {
                if (pm.getFileExtensionListToInclude().contains(file.getExt())) {
                    String text = file.asText();
                    if ("java".equals(file.getExt()) && pm.isExcludeJavadocEnabled()) {
                        text = removeJavadoc(text);
                    }
                    inputForAI.append(text);
                    inputForAI.append("\n");
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        projectContent = inputForAI.toString();
        String response = new JeddictChatModel().generateHtmlDescriptionForProject(projectContent, userQuery);
        displayHtmlContent(removeCodeBlockMarkers(response), null, projectName + " Project GenAI");
    }

    public void askQueryForProjectCommit(Project project, String commitChanges, String intitalCommitMessage) {
        ProjectInformation info = ProjectUtils.getInformation(project);
        String projectName = info.getDisplayName();
        String response = new JeddictChatModel().generateCommitMessageSuggestions(commitChanges, intitalCommitMessage);
        displayHtmlContent(removeCodeBlockMarkers(response), null, projectName + " GenAI Commit");
        this.commitChanges = commitChanges;
    }

    public void askQueryForPackage(Collection<? extends FileObject> selectedPackages, String userQuery) {
        Iterator<? extends FileObject> selectedPackagesIterator = selectedPackages.iterator();
        if (selectedPackagesIterator.hasNext()) {
            this.project = FileOwnerQuery.getOwner(selectedPackagesIterator.next());
            ProjectInformation info = ProjectUtils.getInformation(project);
            String projectName = info.getDisplayName();

            List<FileObject> sourceFiles = selectedPackages.stream()
                    .filter(FileObject::isFolder)
                    .flatMap(packageFolder -> Arrays.stream(packageFolder.getChildren())
                    .filter(FileObject::isData)
                    .filter(file -> pm.getFileExtensionListToInclude().contains(file.getExt())))
                    .collect(Collectors.toList());

            StringBuilder inputForAI = new StringBuilder();
            for (FileObject file : sourceFiles) {
                try {
                    String text = file.asText();
                    if ("java".equals(file.getExt()) && pm.isExcludeJavadocEnabled()) {
                        text = removeJavadoc(text);
                    }
                    inputForAI.append(text);
                    inputForAI.append("\n");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            projectContent = inputForAI.toString();
            String response = new JeddictChatModel().generateHtmlDescriptionForProject(projectContent, userQuery);
            displayHtmlContent(removeCodeBlockMarkers(response), null, projectName + " Package GenAI");
        }
    }

    public void displayHtmlContent(final String response, String filename, String title) {
        SwingUtilities.invokeLater(() -> {
            try {
                File tempFile = File.createTempFile("tempHtml", ".html");
                tempFile.deleteOnExit();
                try (FileWriter writer = new FileWriter(tempFile)) {
                    writer.write(response);
                }

                Preferences prefs = Preferences.userNodeForPackage(AssistantTopComponent.class);
                prefs.putBoolean(AssistantTopComponent.PREFERENCE_KEY, true);
                topComponent = new AssistantTopComponent(title, project);
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

                topComponent.add(createBottomPanel(filename, title, null), BorderLayout.SOUTH);
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

    public void openChat(final String query, String fileName, String title, Consumer<String> action) {
        SwingUtilities.invokeLater(() -> {
            Preferences prefs = Preferences.userNodeForPackage(AssistantTopComponent.class);
            prefs.putBoolean(AssistantTopComponent.PREFERENCE_KEY, true);
            topComponent = new AssistantTopComponent(title, project);
            topComponent.setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(topComponent.getParentPanel());
            topComponent.add(scrollPane, BorderLayout.CENTER);
            topComponent.add(createBottomPanel(fileName, title, action), BorderLayout.SOUTH);
            topComponent.open();
            topComponent.requestActive();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (topComponent != null) {
                    topComponent.close();
                }
            }));
            questionPane.setText(query);
        });
    }

    JEditorPane questionPane;

    private JPanel createBottomPanel(String fileName, String title, Consumer<String> action) {
        // Create a panel for the text field and buttons
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);

        // Panel to hold buttons in a vertical flow (East side)
        JPanel eastButtonPanel = new JPanel();
        eastButtonPanel.setLayout(new BoxLayout(eastButtonPanel, BoxLayout.Y_AXIS));

        Dimension buttonSize = new Dimension(32, 32);  // Set fixed size for buttons

        // Ask Button with Icon (East)
        JButton submitButton = createButton(startIcon);
        submitButton.setToolTipText("Ask a question");
        submitButton.setPreferredSize(buttonSize); // Set button size
        submitButton.setMaximumSize(buttonSize);   // Avoid stretching
        eastButtonPanel.add(submitButton);

        int javaEditorCount = topComponent.getAllJavaEditorCount();

        // Copy Button with Icon (East)
        copyButton = createButton(copyIcon);
        copyButton.setToolTipText("Copy to clipboard");
        copyButton.setPreferredSize(buttonSize);
        copyButton.setMaximumSize(buttonSize);
        copyButton.setEnabled(javaEditorCount > 0);
        eastButtonPanel.add(copyButton);

        // Save Button with Icon (East)
        saveButton = createButton(saveasIcon);
        saveButton.setToolTipText("Save as");
        saveButton.setPreferredSize(buttonSize);
        saveButton.setMaximumSize(buttonSize);
        saveButton.setEnabled(javaEditorCount > 0);
        eastButtonPanel.add(saveButton);

        // New Chat Button (East)
        JButton saveToEditorButton = createButton(saveToEditor);
        saveToEditorButton.setToolTipText("Update " + fileName);
        saveToEditorButton.setPreferredSize(buttonSize);
        saveToEditorButton.setMaximumSize(buttonSize);
        saveToEditorButton.setEnabled(fileName != null);
        eastButtonPanel.add(saveToEditorButton);

        // Add eastButtonPanel in vertical layout to bottomPanel on the EAST side
        gbc.gridx = 2;  // Positioning the button panel on the far right
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.VERTICAL;
        bottomPanel.add(eastButtonPanel, gbc);

        // Panel to hold buttons in a vertical flow (West side)
        JPanel westButtonPanel = new JPanel();
        westButtonPanel.setLayout(new BoxLayout(westButtonPanel, BoxLayout.Y_AXIS));

        // Previous Button (West)
        prevButton = createButton(backIcon);
        prevButton.setToolTipText("Previous Chat");
        prevButton.setPreferredSize(buttonSize);
        prevButton.setMaximumSize(buttonSize);
        westButtonPanel.add(prevButton);

        // Next Button (West)
        nextButton = createButton(forwardIcon);
        nextButton.setToolTipText("Next Chat");
        nextButton.setPreferredSize(buttonSize);
        nextButton.setMaximumSize(buttonSize);
        westButtonPanel.add(nextButton);

        // Up Button to open in browser (West)
        openInBrowserButton = createButton(upIcon);
        openInBrowserButton.setToolTipText("Open in Browser");
        openInBrowserButton.setPreferredSize(buttonSize);
        openInBrowserButton.setMaximumSize(buttonSize);
        openInBrowserButton.setEnabled(topComponent.getAllEditorCount() > 0);
        westButtonPanel.add(openInBrowserButton);

        // New Chat Button (West)
        JButton newChatButton = createButton(newEditor);
        newChatButton.setToolTipText("Start a new chat");
        newChatButton.setPreferredSize(buttonSize);
        newChatButton.setMaximumSize(buttonSize);
        westButtonPanel.add(newChatButton);

        // Add westButtonPanel in vertical layout to bottomPanel on the WEST side
        gbc.gridx = 0;  // Positioning the button panel on the far left
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        bottomPanel.add(westButtonPanel, gbc);

        // JEditorPane instead of JTextField
        questionPane = new JEditorPane();
        questionPane.setEditorKit(createEditorKit("text/x-java"));
        JScrollPane scrollPane = new JScrollPane(questionPane);  // Add scroll if needed
        scrollPane.setPreferredSize(new Dimension(500, 50));
        gbc.gridx = 1;  // Positioning the editor in the middle
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        bottomPanel.add(scrollPane, gbc);

        copyButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(topComponent.getAllJavaEditorText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        saveButton.addActionListener(e -> {
            topComponent.saveAs(topComponent.getAllJavaEditorText());
        });
        saveToEditorButton.addActionListener(e -> {
            if(action != null) {
                action.accept(topComponent.getAllJavaEditorText());
            }
        });
        newChatButton.addActionListener(e -> {
           topComponent.clear();
           topComponent.repaint();
           responseHistory.clear();
           currentResponseIndex = -1;
           updateButtons(prevButton, nextButton);
        });

        // Action for upButton
        openInBrowserButton.addActionListener(e -> {
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
            String question = questionPane.getText();
            if (!question.isEmpty()) {
                submitButton.setIcon(progressIcon);
                submitButton.setEnabled(false);
                handleQuestion(question, submitButton);
            }
        };

        submitButton.addActionListener(submitActionListener);

        prevButton.addActionListener(e -> {
            if (currentResponseIndex > 0) {
                currentResponseIndex--;
                String historyResponse = responseHistory.get(currentResponseIndex);
                updateEditor(historyResponse);
                updateButtons(prevButton, nextButton);
            }
        });

        nextButton.addActionListener(e -> {
            if (currentResponseIndex < responseHistory.size() - 1) {
                currentResponseIndex++;
                String historyResponse = responseHistory.get(currentResponseIndex);
                updateEditor(historyResponse);
                updateButtons(prevButton, nextButton);
            }
        });

        updateButtons(prevButton, nextButton);

        return bottomPanel;
    }
    
    private JButton createButton(ImageIcon icon) {
        JButton button = new JButton(icon);
           // Set button preferred size to match the icon's size (24x24)
        button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));

        // Remove button text and focus painting for a cleaner look
        button.setText(null);
        button.setFocusPainted(false);

        // Set margin to zero for no extra space around the icon
        button.setMargin(new Insets(0, 0, 0, 0));

        // Optional: Remove the button's border if you want a borderless icon
        button.setBorder(BorderFactory.createEmptyBorder());
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Show border on hover
                button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Remove border when not hovering
                button.setBorder(BorderFactory.createEmptyBorder());
            }
        });
        return button;
    }

    private JEditorPane updateEditor(String response) {
        JEditorPane editorPane = null;
        String[] parts = response.split("<pre.*?>\\s*<code type=\"[^\"]*\">|<pre.*?>\\s*<code class=\"[^\"]*\">|<pre.*?>\\s*<code class=\"[^\"]*\" type=\"[^\"]*\">|<pre.*?>\\s*<code type=\"[^\"]*\" class=\"[^\"]*\">|</code>\\s*</pre>");
        if (parts.length == 1) {
            parts = response.split("<code type=\"[^\"]*\">|<code class=\"[^\"]*\">|<code class=\"[^\"]*\" type=\"[^\"]*\">|<code type=\"[^\"]*\" class=\"[^\"]*\">|</code>");
        }
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
                String prevChat = responseHistory.isEmpty() ? null : responseHistory.get(responseHistory.size() - 1);
                if (action == Action.LEARN) {
                    if (responseHistory.size() - 1 == 0) {
                        prevChat = null;
                    }
                }
                if(prevChat != null) {
                    prevChat = topComponent.getAllEditorText();
                    responseHistory.set(responseHistory.size() - 1, prevChat);
                }
                String response;
                if (commitChanges != null) {
                    response = new JeddictChatModel().generateCommitMessageSuggestions(commitChanges, question);
                } else if (treePath == null || projectContent != null) {
                    response = new JeddictChatModel().generateHtmlDescription(projectContent, null, null, prevChat, question);
                } else if (action == Action.TEST) {
                    if (leaf instanceof MethodTree) {
                        response = new JeddictChatModel().generateTestCase(null, null, leaf.toString(), prevChat, question);
                    } else {
                        response = new JeddictChatModel().generateTestCase(null, treePath.getCompilationUnit().toString(), null, prevChat, question);
                    }
                } else {
                    response = new JeddictChatModel().generateHtmlDescription(null, treePath.getCompilationUnit().toString(), treePath.getLeaf() instanceof MethodTree ? treePath.getLeaf().toString() : null, prevChat, question);
                }
                response = removeCodeBlockMarkers(response);
                if (responseHistory.isEmpty() || !response.equals(responseHistory.get(responseHistory.size() - 1))) {
                    responseHistory.add(response);
                    currentResponseIndex = responseHistory.size() - 1;
                }
                updateEditor(response);
                submitButton.setIcon(startIcon);
                submitButton.setEnabled(true);
                saveButton.setEnabled(javaCode != null);
                updateButtons(prevButton, nextButton);
                questionPane.setText("");
            } catch (Exception e) {
                e.printStackTrace();
                submitButton.setIcon(startIcon);
                submitButton.setEnabled(true);
            }
        });
    }

    private void updateButtons(JButton prevButton, JButton nextButton) {
        prevButton.setEnabled(currentResponseIndex > 0);
        nextButton.setEnabled(currentResponseIndex < responseHistory.size() - 1);
        
        int javaEditorCount = topComponent.getAllJavaEditorCount();
        copyButton.setEnabled(javaEditorCount > 0);
        saveButton.setEnabled(javaEditorCount > 0);
        
        openInBrowserButton.setEnabled(topComponent.getAllEditorCount() > 0);
    }

}
