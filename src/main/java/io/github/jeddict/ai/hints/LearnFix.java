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
import io.github.jeddict.ai.JeddictUpdateManager;
import io.github.jeddict.ai.completion.Action;
import io.github.jeddict.ai.lang.JeddictChatModel;
import io.github.jeddict.ai.completion.SQLCompletion;
import io.github.jeddict.ai.components.AssistantTopComponent;
import static io.github.jeddict.ai.components.AssistantTopComponent.createEditorKit;
import io.github.jeddict.ai.settings.PreferencesManager;
import static io.github.jeddict.ai.util.ProjectUtil.getSourceFiles;
import static io.github.jeddict.ai.util.SourceUtil.removeJavadoc;
import io.github.jeddict.ai.util.StringUtil;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ScrollPaneConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.hints.JavaFix;
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
//import static io.github.jeddict.ai.components.AssistantTopComponent.newEditorIcon;
//import static io.github.jeddict.ai.components.AssistantTopComponent.saveToEditorIcon;
//import static io.github.jeddict.ai.components.AssistantTopComponent.settingsIcon;
import io.github.jeddict.ai.components.ContextDialog;
import io.github.jeddict.ai.components.CustomScrollBarUI;
import static io.github.jeddict.ai.components.QueryPane.createIconButton;
import io.github.jeddict.ai.lang.JeddictStreamHandler;
import io.github.jeddict.ai.util.EditorUtil;
import io.github.jeddict.ai.response.Response;
import io.github.jeddict.ai.util.ColorUtil;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getFontFromMimeType;
import static io.github.jeddict.ai.util.Icons.ICON_CONTEXT;
import static io.github.jeddict.ai.util.Icons.ICON_COPY;
import static io.github.jeddict.ai.util.Icons.ICON_NEW_CHAT;
import static io.github.jeddict.ai.util.Icons.ICON_NEXT;
import static io.github.jeddict.ai.util.Icons.ICON_PREV;
import static io.github.jeddict.ai.util.Icons.ICON_SAVE;
import static io.github.jeddict.ai.util.Icons.ICON_SEND;
import static io.github.jeddict.ai.util.Icons.ICON_SETTINGS;
import static io.github.jeddict.ai.util.Icons.ICON_UPDATE;
import static io.github.jeddict.ai.util.Icons.ICON_WEB;
import io.github.jeddict.ai.util.Labels;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import org.netbeans.api.options.OptionsDisplayer;
import java.io.File;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.filesystems.FileUtil;
import org.openide.windows.WindowManager;

/**
 *
 * @author Shiwani Gupta
 */
public class LearnFix extends JavaFix {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TreePath treePath;
    private final Action action;
    private SQLCompletion sqlCompletion;
    private ComponentAdapter buttonPanelAdapter;
    private JButton prevButton, nextButton, copyButton, saveButton, openInBrowserButton;
    private AssistantTopComponent topComponent;
    private JEditorPane questionPane;
    private JScrollPane questionScrollPane;
    private final List<Response> responseHistory = new ArrayList<>();
    private int currentResponseIndex = -1;
    private String sourceCode = null;
    private Project project;
    private List<FileObject> selectedFileObjects = new ArrayList<>();
    private FileObject fileObject;
    private String commitChanges;
    private final PreferencesManager pm = PreferencesManager.getInstance();
    private Tree leaf;

    private Project getProject() {
        if (project != null) {
            return project;
        } else if (selectedFileObjects != null && !selectedFileObjects.isEmpty()) {
            return FileOwnerQuery.getOwner(selectedFileObjects.toArray(FileObject[]::new)[0]);
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

    public LearnFix(Action action, List<FileObject> selectedFileObjects) {
        super(null);
        this.action = action;
        this.selectedFileObjects = selectedFileObjects;
    }

    public LearnFix(Action action, FileObject selectedFileObject) {
        super(null);
        this.action = action;
        this.selectedFileObjects.add(selectedFileObject);
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
                        public void onComplete(String textResponse) {
                            Response response = new Response(null, textResponse);
                            sourceCode = EditorUtil.updateEditors(null, topComponent, response, getContextFiles());
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
                .anyMatch(fo -> fo.getPath().contains("src/main/webapp")
                || fo.getPath().endsWith("src/main")
                );
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

    public void askQueryForProjectCommit(Project project, String commitChanges, String intitalCommitMessage) {
        ProjectInformation info = ProjectUtils.getInformation(project);
        String projectName = info.getDisplayName();
        SwingUtilities.invokeLater(() -> {
            displayHtmlContent(null, projectName + " GenAI Commit");
            JeddictStreamHandler handler = new JeddictStreamHandler(topComponent) {
                @Override
                public void onComplete(String textResponse) {
                    Response response = new Response(null, textResponse);
                    sourceCode = EditorUtil.updateEditors(null, topComponent, response, getContextFiles());
                    responseHistory.add(response);
                    currentResponseIndex = responseHistory.size() - 1;
                    LearnFix.this.commitChanges = commitChanges;
                }
            };
            String response = new JeddictChatModel(handler).generateCommitMessageSuggestions(commitChanges, intitalCommitMessage);
            if (response != null && !response.isEmpty()) {
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
        topComponent.add(createBottomPanel(null, filename, null), BorderLayout.SOUTH);
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
            Color bgColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
            boolean isDark = ColorUtil.isDarkColor(bgColor);
            if (isDark) {
                scrollPane.getViewport().setBackground(Color.DARK_GRAY);
                scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
                scrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
            }
            topComponent.add(scrollPane, BorderLayout.CENTER);
            topComponent.add(createBottomPanel(type, fileName, action), BorderLayout.SOUTH);
            if (PreferencesManager.getInstance().getChatPlacement().equals("Left")) {
                WindowManager.getDefault()
                        .findMode("explorer")
                        .dockInto(topComponent);
            } else if (PreferencesManager.getInstance().getChatPlacement().equals("Right")) {
                WindowManager.getDefault()
                        .findMode("properties")
                        .dockInto(topComponent);
            }
            topComponent.open();
            topComponent.requestActive();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (topComponent != null) {
                    SwingUtilities.invokeLater(() -> topComponent.close());
                }
            }));
            questionPane.setText(query);
            initialMessage();
        });
    }

    private void initialMessage() {
        topComponent.createHtmlPane(
                "<div style='margin:20px; padding:20px; border-radius:10px;'>"
                + "<div style='text-align:center;'>"
                + "👋 <strong>Welcome!</strong><br><br>"
                + "I'm here to assist you with any questions you have.<br>"
                + "Feel free to ask anything!<br>"
                + "</div>"
                + "</div>"
        );
        EventQueue.invokeLater(() -> questionPane.requestFocusInWindow());
    }

    private JPanel createBottomPanel(String type, String fileName, Consumer<String> action) {
        JPanel bottomPanel = new JPanel(new BorderLayout());

        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder());

        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 1));
        rightButtonPanel.setBackground(backgroundColor);
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder());
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        JPanel leftButtonPanel = new JPanel();
        leftButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 1));
        leftButtonPanel.setBackground(backgroundColor);
        leftButtonPanel.setBorder(BorderFactory.createEmptyBorder());
        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);

        prevButton = createIconButton(Labels.PREV, ICON_PREV);
        prevButton.setToolTipText("Previous Chat");
        leftButtonPanel.add(prevButton);

        nextButton = createIconButton(Labels.NEXT, ICON_NEXT);
        nextButton.setToolTipText("Next Chat");
        leftButtonPanel.add(nextButton);

        openInBrowserButton = createIconButton(Labels.VIEW, ICON_WEB);
        openInBrowserButton.setToolTipText("Open in Browser");
        openInBrowserButton.setVisible(topComponent.getAllEditorCount() > 0);
        leftButtonPanel.add(openInBrowserButton);

        int javaEditorCount = topComponent.getAllCodeEditorCount();

        copyButton = createIconButton(Labels.COPY, ICON_COPY);
        copyButton.setToolTipText("Copy to clipboard");
        copyButton.setVisible(javaEditorCount > 0);
        leftButtonPanel.add(copyButton);

        saveButton = createIconButton(Labels.SAVE, ICON_SAVE);
        saveButton.setToolTipText("Save as");
        saveButton.setVisible(javaEditorCount > 0);
        leftButtonPanel.add(saveButton);

        JButton saveToEditorButton = createIconButton(Labels.UPDATE + " " + fileName, ICON_UPDATE);
        saveToEditorButton.setToolTipText("Update " + fileName);
        saveToEditorButton.setVisible(fileName != null);
        leftButtonPanel.add(saveToEditorButton);

        JButton optionsButton = createIconButton(Labels.SETTINGS, ICON_SETTINGS);
        optionsButton.setToolTipText("Open Jeddict AI Assistant Settings");
        optionsButton.addActionListener(e -> OptionsDisplayer.getDefault().open("JeddictAIAssistant"));
        rightButtonPanel.add(optionsButton);

        JButton contextButton = createIconButton(Labels.CONTEXT, ICON_CONTEXT);
        contextButton.setToolTipText("View detailed context of the current chat session.");
        contextButton.addActionListener(e -> showFilePathPopup());
        rightButtonPanel.add(contextButton);

        JButton newChatButton = createIconButton(Labels.NEW_CHAT, ICON_NEW_CHAT);
        newChatButton.setToolTipText("Start a new chat");
        leftButtonPanel.add(newChatButton);

        JButton submitButton = createIconButton(Labels.SEND, ICON_SEND);
        submitButton.setToolTipText("Ask a question");
        rightButtonPanel.add(submitButton);

        buttonPanelAdapter = new ComponentAdapter() {
            boolean showOnlyIcons = false;

            @Override
            public void componentResized(ComponentEvent e) {
                double totalButtonWidth = leftButtonPanel.getPreferredSize().width
                        + rightButtonPanel.getPreferredSize().width;
                if (showOnlyIcons) {
                    totalButtonWidth = totalButtonWidth * 2;
                }

                int availableWidth = buttonPanel.getWidth();
                showOnlyIcons = availableWidth < totalButtonWidth;

                updateButton(prevButton, showOnlyIcons, ICON_PREV, Labels.PREV + " " + ICON_PREV);
                updateButton(nextButton, showOnlyIcons, ICON_NEXT, Labels.NEXT + " " + ICON_NEXT);
                updateButton(openInBrowserButton, showOnlyIcons, ICON_WEB, Labels.VIEW + " " + ICON_WEB);
                updateButton(copyButton, showOnlyIcons, ICON_COPY, Labels.COPY + " " + ICON_COPY);
                updateButton(saveButton, showOnlyIcons, ICON_SAVE, Labels.SAVE + " " + ICON_SAVE);
                updateButton(saveToEditorButton, showOnlyIcons, ICON_UPDATE, Labels.UPDATE + " " + ICON_UPDATE);
                updateButton(newChatButton, showOnlyIcons, ICON_NEW_CHAT, Labels.NEW_CHAT + " " + ICON_NEW_CHAT);
                updateButton(optionsButton, showOnlyIcons, ICON_SETTINGS, Labels.SETTINGS + " " + ICON_SETTINGS);
                updateButton(contextButton, showOnlyIcons, ICON_CONTEXT, Labels.CONTEXT + " " + ICON_CONTEXT);
                updateButton(submitButton, showOnlyIcons, ICON_SEND, Labels.SEND + " " + ICON_SEND);

                topComponent.updateUserPaneButtons(showOnlyIcons);

            }

            private void updateButton(JButton button, boolean iconOnly, String iconText, String fullText) {
                button.setText(iconOnly ? iconText : fullText);
            }
        };
        buttonPanel.addComponentListener(buttonPanelAdapter);

        questionPane = new JEditorPane();
        questionPane.setEditorKit(createEditorKit("text/x-" + (type == null ? "java" : type)));
        questionScrollPane = new JScrollPane(questionPane);
        questionScrollPane.setBorder(BorderFactory.createEmptyBorder());
        questionScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        questionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Color bgColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        boolean isDark = ColorUtil.isDarkColor(bgColor);
        if (isDark) {
            questionScrollPane.getViewport().setBackground(Color.DARK_GRAY);
            questionScrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
            questionScrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        }
        questionPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateHeight();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateHeight();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateHeight();
            }
        });

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
            initialMessage();
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
                submitButton.setText("...️");
                submitButton.setEnabled(false);
                handleQuestion(question, submitButton, true);
            }
        };

        submitButton.addActionListener(submitActionListener);

        Consumer<String> queryUpdate = newQuery -> {
            handleQuestion(newQuery, submitButton, false);
        };
        prevButton.addActionListener(e -> {
            if (currentResponseIndex > 0) {
                currentResponseIndex--;
                Response historyResponse = responseHistory.get(currentResponseIndex);
                sourceCode = EditorUtil.updateEditors(queryUpdate, topComponent, historyResponse, getContextFiles());
                updateButtons(prevButton, nextButton);
            }
        });

        nextButton.addActionListener(e -> {
            if (currentResponseIndex < responseHistory.size() - 1) {
                currentResponseIndex++;
                Response historyResponse = responseHistory.get(currentResponseIndex);
                sourceCode = EditorUtil.updateEditors(queryUpdate, topComponent, historyResponse, getContextFiles());
                updateButtons(prevButton, nextButton);
            }
        });

        updateButtons(prevButton, nextButton);

        TransferHandler defaultHandler = questionPane.getTransferHandler();
        questionPane.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                try {
                    Transferable t = support.getTransferable();
                    for (DataFlavor flavor : t.getTransferDataFlavors()) {
                        if (flavor.isFlavorJavaFileListType()
                                || DataObject[].class.equals(flavor.getRepresentationClass())
                                || Node.class.isAssignableFrom(flavor.getRepresentationClass())) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return defaultHandler.canImport(support);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    Transferable t = support.getTransferable();
                    for (DataFlavor flavor : t.getTransferDataFlavors()) {
                        if (flavor.isFlavorJavaFileListType()) {
                            List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                            for (File file : files) {
                                selectedFileObjects.add(FileUtil.toFileObject(file));
                            }
                            return true;
                        }
                        if (DataObject[].class.equals(flavor.getRepresentationClass())) {
                            DataObject[] dataObjects = (DataObject[]) t.getTransferData(flavor);
                            for (DataObject dobj : dataObjects) {
                                if (dobj != null) {
                                    selectedFileObjects.add(dobj.getPrimaryFile());
                                }
                            }
                            return true;
                        }
                        if (Node.class.isAssignableFrom(flavor.getRepresentationClass())) {
                            Node node = (Node) t.getTransferData(flavor);
                            DataObject dobj = node.getLookup().lookup(DataObject.class);
                            if (dobj != null) {
                                selectedFileObjects.add(dobj.getPrimaryFile());
                            }
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return defaultHandler.importData(support);
            }

            @Override
            public void exportToClipboard(JComponent comp, Clipboard clipboard, int action) throws IllegalStateException {
                defaultHandler.exportToClipboard(comp, clipboard, action);
            }
        });

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(questionScrollPane);       // Will honor max height
        bottomPanel.add(javax.swing.Box.createVerticalStrut(0)); // spacing
        bottomPanel.add(buttonPanel);      // Compact button row

        return bottomPanel;
    }

    private void updateHeight() {
        int lineHeight = questionPane.getFontMetrics(questionPane.getFont()).getHeight();
        String text = questionPane.getText().trim();
        int preferredHeight;
        int linePadding = 5;
        if (text.isEmpty()) {
            preferredHeight = lineHeight * 3 + linePadding;
        } else {
            preferredHeight = questionPane.getPreferredSize().height;
            preferredHeight = Math.max(lineHeight * 3 + linePadding,
                    Math.min(preferredHeight, lineHeight * 12 + linePadding));
        }

        Dimension size = new Dimension(questionScrollPane.getWidth(), preferredHeight);
        questionScrollPane.setPreferredSize(size);
        questionScrollPane.revalidate();
    }

    private List<FileObject> getContextFiles() {
        List<FileObject> fileObjects;
        if (project != null) {
            fileObjects = getProjectContextList();
        } else if (selectedFileObjects != null) {
            fileObjects = getFilesContextList();
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

    private void handleQuestion(String question, JButton submitButton, boolean newQuery) {
        executorService.submit(() -> {
            try {
                if (currentResponseIndex >= 0
                        && currentResponseIndex + 1 < responseHistory.size()) {
                    responseHistory.subList(currentResponseIndex + 1, responseHistory.size()).clear();
                }
                if (!newQuery && !responseHistory.isEmpty()) {
                    responseHistory.remove(responseHistory.size() - 1);
                    currentResponseIndex = currentResponseIndex - 1;
                }
                Response prevChat = responseHistory.isEmpty() ? null : responseHistory.get(responseHistory.size() - 1);
                if (action == Action.LEARN) {
                    if (responseHistory.size() - 1 == 0) {
                        prevChat = null;
                    }
                }
                if (prevChat != null) {
                    prevChat = responseHistory.get(currentResponseIndex);
                }
                questionPane.setText("");
                updateHeight();
                JeddictStreamHandler handler = new JeddictStreamHandler(topComponent) {
                    @Override
                    public void onComplete(String textResponse) {
                        Response response = new Response(question, textResponse);
                        if (responseHistory.isEmpty() || !textResponse.equals(responseHistory.get(responseHistory.size() - 1))) {
                            responseHistory.add(response);
                            currentResponseIndex = responseHistory.size() - 1;
                        }
                        SwingUtilities.invokeLater(() -> {
                            Consumer<String> queryUpdate = newQuery -> {
                                handleQuestion(newQuery, submitButton, false);
                            };

                            sourceCode = EditorUtil.updateEditors(queryUpdate, topComponent, response, getContextFiles());
                            buttonPanelAdapter.componentResized(null);
                            submitButton.setEnabled(true);
                            saveButton.setVisible(sourceCode != null);
                            updateButtons(prevButton, nextButton);
                        });
                    }
                };
                String response;
                if (sqlCompletion != null) {
                    response = new JeddictChatModel(handler).assistDbMetadata(sqlCompletion.getMetaData(), question);
                } else if (commitChanges != null) {
                    response = new JeddictChatModel(handler).generateCommitMessageSuggestions(commitChanges, question);
                } else if (project != null) {
                    response = new JeddictChatModel(handler).generateDescription(getProject(), getProjectContext(), null, prevChat, question);
                } else if (selectedFileObjects != null) {
                    response = new JeddictChatModel(handler).generateDescription(getProject(), getFilesContext(), null, prevChat, question);
                } else if (treePath == null) {
                    response = new JeddictChatModel(handler).generateDescription(getProject(), null, null, prevChat, question);
                } else if (action == Action.TEST) {
                    if (leaf instanceof MethodTree) {
                        response = new JeddictChatModel(handler).generateTestCase(getProject(), null, null, leaf.toString(), prevChat, question);
                    } else {
                        response = new JeddictChatModel(handler).generateTestCase(getProject(), null, treePath.getCompilationUnit().toString(), null, prevChat, question);
                    }
                } else {
                    response = new JeddictChatModel(handler).generateDescription(getProject(), treePath.getCompilationUnit().toString(), treePath.getLeaf() instanceof MethodTree ? treePath.getLeaf().toString() : null, prevChat, question);
                }

                if (response != null && !response.isEmpty()) {
//                    response = removeCodeBlockMarkers(response);
                    handler.onComplete(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                buttonPanelAdapter.componentResized(null);
                submitButton.setEnabled(true);
            }
        });
    }

    private void updateButtons(JButton prevButton, JButton nextButton) {
        prevButton.setVisible(currentResponseIndex > 0);
        nextButton.setVisible(currentResponseIndex < responseHistory.size() - 1);

        int javaEditorCount = topComponent.getAllCodeEditorCount();
        copyButton.setVisible(javaEditorCount > 0);
        saveButton.setVisible(javaEditorCount > 0);

        openInBrowserButton.setVisible(topComponent.getAllEditorCount() > 0);
    }

}
