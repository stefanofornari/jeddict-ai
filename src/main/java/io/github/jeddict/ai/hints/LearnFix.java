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
package io.github.jeddict.ai.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import static com.sun.source.tree.Tree.Kind.CLASS;
import static com.sun.source.tree.Tree.Kind.ENUM;
import static com.sun.source.tree.Tree.Kind.INTERFACE;
import static com.sun.source.tree.Tree.Kind.METHOD;
import com.sun.source.util.TreePath;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import io.github.jeddict.ai.JeddictUpdateManager;
import io.github.jeddict.ai.completion.Action;
import io.github.jeddict.ai.lang.JeddictChatModel;
import io.github.jeddict.ai.completion.SQLCompletion;
import io.github.jeddict.ai.components.AssistantTopComponent;
import static io.github.jeddict.ai.components.AssistantTopComponent.attachIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.backIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.copyIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.createEditorKit;
import static io.github.jeddict.ai.components.AssistantTopComponent.forwardIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.saveasIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.startIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.upIcon;
import io.github.jeddict.ai.settings.PreferencesManager;
import static io.github.jeddict.ai.util.ProjectUtil.getSourceFiles;
import static io.github.jeddict.ai.util.SourceUtil.removeJavadoc;
import io.github.jeddict.ai.util.StringUtil;
import static io.github.jeddict.ai.util.StringUtil.removeCodeBlockMarkers;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.newEditorIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.progressIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.saveToEditorIcon;
import static io.github.jeddict.ai.components.AssistantTopComponent.settingsIcon;
import io.github.jeddict.ai.components.ContextDialog;
import io.github.jeddict.ai.lang.JeddictStreamHandler;
import io.github.jeddict.ai.util.EditorUtil;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.netbeans.api.options.OptionsDisplayer;

/**
 *
 * @author Shiwani Gupta
 */
public class LearnFix extends JavaFix {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TreePath treePath;
    private final Action action;
    private SQLCompletion sqlCompletion;
    private JButton prevButton, nextButton, copyButton, saveButton, openInBrowserButton;
    private AssistantTopComponent topComponent;
    private final List<String> responseHistory = new ArrayList<>();
    private int currentResponseIndex = -1;
    private String sourceCode = null;
    private Project project;
    private Collection<? extends FileObject> selectedFileObjects;
    private FileObject selectedFileObject;
    private FileObject fileObject;
    private String commitChanges;
    private PreferencesManager pm = PreferencesManager.getInstance();
    private Tree leaf;

    private Project getProject() {
        if (project != null) {
            return project;
        } else if (selectedFileObjects != null && !selectedFileObjects.isEmpty()) {
            return FileOwnerQuery.getOwner(selectedFileObjects.toArray(FileObject[]::new)[0]);
        } else if (selectedFileObject != null) {
            return FileOwnerQuery.getOwner(selectedFileObject);
        } else if (fileObject != null) {
            return FileOwnerQuery.getOwner(fileObject);
        } else {
            return null;
        }
    }

    public LearnFix(TreePathHandle tpHandle, Action action, TreePath treePath) {
        super(tpHandle);
        this.treePath = treePath;
        this.action = action;
    }

    public LearnFix(Action action) {
        super(null);
        this.action = action;
    }

    public LearnFix(Action action, SQLCompletion sqlCompletion) {
        super(null);
        this.action = action;
        this.sqlCompletion = sqlCompletion;
    }

    public LearnFix(Action action, Project project) {
        super(null);
        this.action = action;
        this.project = project;
    }

    public LearnFix(Action action, Collection<? extends FileObject> selectedFileObjects) {
        super(null);
        this.action = action;
        this.selectedFileObjects = selectedFileObjects;
    }

    public LearnFix(Action action, FileObject selectedFileObject) {
        super(null);
        this.action = action;
        this.selectedFileObject = selectedFileObject;
    }

    @Override
    protected String getText() {
        if (action == Action.LEARN) {
            return NbBundle.getMessage(JeddictUpdateManager.class, "HINT_LEARN",
                    StringUtil.convertToCapitalized(treePath.getLeaf().getKind().toString()));
        } else if (action == Action.QUERY) {
            return NbBundle.getMessage(JeddictUpdateManager.class, "HINT_QUERY",
                    StringUtil.convertToCapitalized(treePath.getLeaf().getKind().toString()));
        } else if (action == Action.TEST) {
            return NbBundle.getMessage(JeddictUpdateManager.class, "HINT_TEST",
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
        this.fileObject = copy.getFileObject();

        if (leaf.getKind() == CLASS
                || leaf.getKind() == INTERFACE
                || leaf.getKind() == ENUM
                || leaf.getKind() == METHOD) {
            executorService.submit(() -> {
                String name;
                if (leaf instanceof MethodTree) {
                    name = ((MethodTree) leaf).getName().toString();
                } else {
                    name = ((ClassTree) leaf).getSimpleName().toString();
                }
                String fileName = fileObject != null ? fileObject.getName() : null;

                SwingUtilities.invokeLater(() -> {
                    displayHtmlContent(fileName, name + " AI Assistant");
                    JeddictStreamHandler handler = new JeddictStreamHandler(topComponent) {
                        @Override
                        public void onComplete(String response) {
                            sourceCode = EditorUtil.updateEditors(topComponent, response, getContextFiles());
                            responseHistory.add(response);
                            currentResponseIndex = responseHistory.size() - 1;
                        }
                    };
                    String response;
                    if (action == Action.TEST) {
                        if (leaf instanceof MethodTree) {
                            response = new JeddictChatModel(handler).generateTestCase(getProject(), null, null, leaf.toString(), null, null);
                        } else {
                            response = new JeddictChatModel(handler).generateTestCase(getProject(), null, treePath.getCompilationUnit().toString(), null, null, null);
                        }
                    } else {
                        if (leaf instanceof MethodTree) {
                            response = new JeddictChatModel(handler).assistJavaMethod(getProject(), leaf.toString());
                        } else {
                            response = new JeddictChatModel(handler).assistJavaClass(getProject(), treePath.getCompilationUnit().toString());
                        }
                    }
                    if (response != null && !response.isEmpty()) {
//                        response = removeCodeBlockMarkers(response);
                        handler.onComplete(response);
                    }
                });

            });
        }
    }

    private List<FileObject> getProjectContextList() {
        return getSourceFiles(project);
    }

    private String getProjectContext() {
        StringBuilder inputForAI = new StringBuilder();
        for (FileObject file : getProjectContextList()) {
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

        return inputForAI.toString();
    }

    public String getFilesContext() {
        StringBuilder inputForAI = new StringBuilder();
        for (FileObject file : getFilesContextList()) {
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
        return inputForAI.toString();
    }

    public List<FileObject> getFilesContextList() {
        List<FileObject> sourceFiles = new ArrayList<>();
        boolean includeNestedFiles = selectedFileObjects.stream()
                .anyMatch(fileObject -> fileObject.getPath().contains("src/main/webapp")
                || fileObject.getPath().endsWith("src/main")
                );

        for (FileObject sourceFile : selectedFileObjects) {
            System.out.println("");
        }
        // Function to collect files recursively
        if (includeNestedFiles) {
            for (FileObject selectedFile : selectedFileObjects) {
                if (selectedFile.isFolder()) {
                    collectNestedFiles(selectedFile, sourceFiles);
                } else if (selectedFile.isData() && pm.getFileExtensionListToInclude().contains(selectedFile.getExt())) {
                    sourceFiles.add(selectedFile);
                }
            }
        } else {
            // Collect only immediate children files
            sourceFiles.addAll(selectedFileObjects.stream()
                    .filter(FileObject::isFolder)
                    .flatMap(packageFolder -> Arrays.stream(packageFolder.getChildren())
                    .filter(FileObject::isData)
                    .filter(file -> pm.getFileExtensionListToInclude().contains(file.getExt())))
                    .collect(Collectors.toList()));

            sourceFiles.addAll(selectedFileObjects.stream()
                    .filter(FileObject::isData)
                    .filter(file -> pm.getFileExtensionListToInclude().contains(file.getExt()))
                    .collect(Collectors.toList()));
        }

        return sourceFiles;
    }

    private void collectNestedFiles(FileObject folder, List<FileObject> sourceFiles) {
        // Collect immediate data files
        Arrays.stream(folder.getChildren())
                .filter(FileObject::isData)
                .filter(file -> pm.getFileExtensionListToInclude().contains(file.getExt()))
                .forEach(sourceFiles::add);

        // Recursively collect from subfolders
        Arrays.stream(folder.getChildren())
                .filter(FileObject::isFolder)
                .forEach(subFolder -> collectNestedFiles(subFolder, sourceFiles));
    }

    public String getFileContext() {
        StringBuilder inputForAI = new StringBuilder();
        try {
            String text = selectedFileObject.asText();
//            if ("java".equals(selectedFileObject.getExt()) && pm.isExcludeJavadocEnabled()) {
//                text = removeJavadoc(text);
//            }
            inputForAI.append(text);
            inputForAI.append("\n");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return inputForAI.toString();
    }

    public void askQueryForProjectCommit(Project project, String commitChanges, String intitalCommitMessage) {
        ProjectInformation info = ProjectUtils.getInformation(project);
        String projectName = info.getDisplayName();
        SwingUtilities.invokeLater(() -> {
            displayHtmlContent(null, projectName + " GenAI Commit");
            JeddictStreamHandler handler = new JeddictStreamHandler(topComponent) {
                @Override
                public void onComplete(String response) {
//                    response = removeCodeBlockMarkers(response);
                    sourceCode = EditorUtil.updateEditors(topComponent, response, getContextFiles());
                    responseHistory.add(response);
                    currentResponseIndex = responseHistory.size() - 1;
                    LearnFix.this.commitChanges = commitChanges;
                }
            };
            String response = new JeddictChatModel(handler).generateCommitMessageSuggestions(commitChanges, intitalCommitMessage);
            if (response != null && !response.isEmpty()) {
//                response = removeCodeBlockMarkers(response);
                handler.onComplete(response);
            }
        });
    }

    public void displayHtmlContent(String filename, String title) {
        Preferences prefs = Preferences.userNodeForPackage(AssistantTopComponent.class);
        prefs.putBoolean(AssistantTopComponent.PREFERENCE_KEY, true);
        topComponent = new AssistantTopComponent(title, null, getProject());
        JScrollPane scrollPane = new JScrollPane(topComponent.getParentPanel());
        topComponent.add(scrollPane, BorderLayout.CENTER);
        topComponent.add(createBottomPanel(null, filename, title, null), BorderLayout.SOUTH);
        topComponent.open();
        topComponent.requestActive();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                if (topComponent != null) {
                    topComponent.close();
                }
            });
        }));
    }

    public void openChat(String type, final String query, String fileName, String title, Consumer<String> action) {
        SwingUtilities.invokeLater(() -> {
            new JeddictUpdateManager().checkForJeddictUpdate();
            Preferences prefs = Preferences.userNodeForPackage(AssistantTopComponent.class);
            prefs.putBoolean(AssistantTopComponent.PREFERENCE_KEY, true);
            topComponent = new AssistantTopComponent(title, type, getProject());
            topComponent.setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(topComponent.getParentPanel());
            topComponent.add(scrollPane, BorderLayout.CENTER);
            topComponent.add(createBottomPanel(type, fileName, title, action), BorderLayout.SOUTH);
            topComponent.open();
            topComponent.requestActive();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (topComponent != null) {
                    SwingUtilities.invokeLater(() -> topComponent.close());
                }
            }));
            questionPane.setText(query);
        });
    }

    JEditorPane questionPane;

    private JPanel createBottomPanel(String type, String fileName, String title, Consumer<String> action) {
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
        submitButton.setPreferredSize(buttonSize);
        submitButton.setMaximumSize(buttonSize);
        eastButtonPanel.add(submitButton);

        int javaEditorCount = topComponent.getAllCodeEditorCount();

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
        JButton saveToEditorButton = createButton(saveToEditorIcon);
        saveToEditorButton.setToolTipText("Update " + fileName);
        saveToEditorButton.setPreferredSize(buttonSize);
        saveToEditorButton.setMaximumSize(buttonSize);
        saveToEditorButton.setEnabled(fileName != null);
        eastButtonPanel.add(saveToEditorButton);

        gbc.gridx = 2;
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

        JButton optionsButton = createButton(settingsIcon); // Replace with actual icon path
        optionsButton.setToolTipText("Open Jeddict AI Assistant Settings");
        optionsButton.setPreferredSize(buttonSize);
        optionsButton.setMaximumSize(buttonSize);
        optionsButton.addActionListener(e -> {
            OptionsDisplayer.getDefault().open("JeddictAIAssistant");
        });
        westButtonPanel.add(optionsButton);

        // Jeddict Button (West)
//        JButton jeddictButton = createButton(logoIcon);
//        jeddictButton.addActionListener(e -> {
//            try {
//                Desktop.getDesktop().browse(new URI("https://jeddict.github.io/page.html?l=tutorial/AI"));
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
//        westButtonPanel.add(jeddictButton);
        // New Chat Button (West)
        JButton newChatButton = createButton(newEditorIcon);
        newChatButton.setToolTipText("Start a new chat");
        newChatButton.setPreferredSize(buttonSize);
        newChatButton.setMaximumSize(buttonSize);
        westButtonPanel.add(newChatButton);

        // Add westButtonPanel in vertical layout to bottomPanel on the WEST side
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        bottomPanel.add(westButtonPanel, gbc);

        // JEditorPane instead of JTextField
        questionPane = new JEditorPane();
        questionPane.setEditorKit(createEditorKit("text/x-" + (type == null ? "java" : type)));
        JScrollPane scrollPane = new JScrollPane(questionPane);
        scrollPane.setPreferredSize(new Dimension(500, 50));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        bottomPanel.add(scrollPane, gbc);

        // Context Button (East)
        JButton contextButton = createButton(attachIcon);
        contextButton.setToolTipText("View detailed context of the current chat session.");
        contextButton.addActionListener(e -> {
            // Code to open popup with JTable
            showFilePathPopup();
        });
        eastButtonPanel.add(contextButton);

        copyButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(topComponent.getAllCodeEditorText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        saveButton.addActionListener(e -> {
            topComponent.saveAs(null, topComponent.getAllCodeEditorText());
        });
        saveToEditorButton.addActionListener(e -> {
            if (action != null) {
                action.accept(topComponent.getAllCodeEditorText());
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
                File latestTempFile = File.createTempFile("gen-ai", ".html");
                latestTempFile.deleteOnExit();
                try (FileWriter writer = new FileWriter(latestTempFile)) {
                    writer.write(topComponent.getAllEditorText());
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
                sourceCode = EditorUtil.updateEditors(topComponent, historyResponse, getContextFiles());
                updateButtons(prevButton, nextButton);
            }
        });

        nextButton.addActionListener(e -> {
            if (currentResponseIndex < responseHistory.size() - 1) {
                currentResponseIndex++;
                String historyResponse = responseHistory.get(currentResponseIndex);
                sourceCode = EditorUtil.updateEditors(topComponent, historyResponse, getContextFiles());
                updateButtons(prevButton, nextButton);
            }
        });

        updateButtons(prevButton, nextButton);

        return bottomPanel;
    }

    private List<FileObject> getContextFiles() {
          List<FileObject> fileObjects;
        if (project != null) {
            fileObjects = getProjectContextList();
        } else if (selectedFileObjects != null) {
            fileObjects = getFilesContextList();
        } else if (selectedFileObject != null) {
            fileObjects = Collections.singletonList(selectedFileObject);
        } else {
            fileObjects = Collections.EMPTY_LIST;
        }
        return fileObjects;
    }
    private void showFilePathPopup() {
        List<FileObject> fileObjects = getContextFiles();
        String projectRootDir = null;
        if (project != null) {
            projectRootDir = project.getProjectDirectory().getPath();
        } else if (!fileObjects.isEmpty()) {
            projectRootDir = FileOwnerQuery.getOwner(fileObjects.get(0)).getProjectDirectory().getPath();
        }

        boolean enableRules = true;
        String rules = pm.getCommonPromptRules();
        if (commitChanges != null) {
            rules = commitChanges;
            enableRules = false;
        }
        ContextDialog dialog = new ContextDialog((JFrame) SwingUtilities.windowForComponent(topComponent),
                enableRules, rules,
                projectRootDir, fileObjects);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(800, 800);
        dialog.setLocationRelativeTo(SwingUtilities.windowForComponent(topComponent));
        dialog.setVisible(true);
        if (commitChanges == null) {
            pm.setCommonPromptRules(dialog.getRules());
        }
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

    private void handleQuestion(String question, JButton submitButton) {
        executorService.submit(() -> {
            try {
                String prevChat = responseHistory.isEmpty() ? null : responseHistory.get(responseHistory.size() - 1);
                if (action == Action.LEARN) {
                    if (responseHistory.size() - 1 == 0) {
                        prevChat = null;
                    }
                }
                if (prevChat != null) {
                    prevChat = responseHistory.get(currentResponseIndex);
                }
                JeddictStreamHandler handler = new JeddictStreamHandler(topComponent) {
                    @Override
                    public void onComplete(String response) {
                        if (responseHistory.isEmpty() || !response.equals(responseHistory.get(responseHistory.size() - 1))) {
                            responseHistory.add(response);
                            currentResponseIndex = responseHistory.size() - 1;
                        }
                        String finalResponse = response;
                        SwingUtilities.invokeLater(() -> {
                            sourceCode = EditorUtil.updateEditors(topComponent, finalResponse, getContextFiles());
                            submitButton.setIcon(startIcon);
                            submitButton.setEnabled(true);
                            saveButton.setEnabled(sourceCode != null);
                            updateButtons(prevButton, nextButton);
                            questionPane.setText("");
                        });
                    }
                };
                String response;
                if (sqlCompletion != null) {
                    response = new JeddictChatModel(handler).assistDbMetadata(sqlCompletion.getMetaData(), question);
                } else if (commitChanges != null) {
                    response = new JeddictChatModel(handler).generateCommitMessageSuggestions(commitChanges, question);
                } else if (project != null) {
                    response = new JeddictChatModel(handler).generateDescription(getProject(), getProjectContext(), null, null, prevChat, question);
                } else if (selectedFileObjects != null) {
                    response = new JeddictChatModel(handler).generateDescription(getProject(), getFilesContext(), null, null, prevChat, question);
                } else if (selectedFileObject != null) {
                    response = new JeddictChatModel(handler).generateDescription(getProject(), null, getFileContext(), null, prevChat, question);
                } else if (treePath == null) {
                    response = new JeddictChatModel(handler).generateDescription(getProject(), null, null, null, prevChat, question);
                } else if (action == Action.TEST) {
                    if (leaf instanceof MethodTree) {
                        response = new JeddictChatModel(handler).generateTestCase(getProject(), null, null, leaf.toString(), prevChat, question);
                    } else {
                        response = new JeddictChatModel(handler).generateTestCase(getProject(), null, treePath.getCompilationUnit().toString(), null, prevChat, question);
                    }
                } else {
                    response = new JeddictChatModel(handler).generateDescription(getProject(), null, treePath.getCompilationUnit().toString(), treePath.getLeaf() instanceof MethodTree ? treePath.getLeaf().toString() : null, prevChat, question);
                }

                if (response != null && !response.isEmpty()) {
//                    response = removeCodeBlockMarkers(response);
                    handler.onComplete(response);
                }
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

        int javaEditorCount = topComponent.getAllCodeEditorCount();
        copyButton.setEnabled(javaEditorCount > 0);
        saveButton.setEnabled(javaEditorCount > 0);

        openInBrowserButton.setEnabled(topComponent.getAllEditorCount() > 0);
    }

}
