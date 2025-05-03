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
import static io.github.jeddict.ai.classpath.JeddictQueryCompletionQuery.JEDDICT_EDITOR_CALLBACK;
import io.github.jeddict.ai.completion.Action;
import io.github.jeddict.ai.lang.JeddictChatModel;
import io.github.jeddict.ai.completion.SQLCompletion;
import io.github.jeddict.ai.components.AssistantTopComponent;
import static io.github.jeddict.ai.components.AssistantTopComponent.createEditorKit;
import io.github.jeddict.ai.settings.PreferencesManager;
import static io.github.jeddict.ai.util.ProjectUtil.getSourceFiles;
import io.github.jeddict.ai.util.StringUtil;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;
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
import org.openide.util.NbBundle;
import java.awt.Color;
import java.awt.Dimension;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import io.github.jeddict.ai.components.ContextDialog;
import io.github.jeddict.ai.components.CustomScrollBarUI;
import io.github.jeddict.ai.components.FileTab;
import io.github.jeddict.ai.components.FileTransferHandler;
import static io.github.jeddict.ai.components.MarkdownPane.getHtmlWrapWidth;
import io.github.jeddict.ai.components.MessageContextComponentAdapter;
import io.github.jeddict.ai.components.QueryPane;
import io.github.jeddict.ai.components.QueryPane.RoundedBorder;
import static io.github.jeddict.ai.components.QueryPane.createIconButton;
import static io.github.jeddict.ai.components.QueryPane.createStyledComboBox;
import io.github.jeddict.ai.components.TokenUsageChartDialog;
import static io.github.jeddict.ai.util.ContextHelper.getFilesContextList;
import static io.github.jeddict.ai.util.ContextHelper.getImageFilesContext;
import static io.github.jeddict.ai.util.ContextHelper.getProjectContext;
import static io.github.jeddict.ai.util.ContextHelper.getTextFilesContext;
import io.github.jeddict.ai.lang.JeddictStreamHandler;
import io.github.jeddict.ai.util.EditorUtil;
import io.github.jeddict.ai.response.Response;
import io.github.jeddict.ai.settings.GenAIProvider;
import static io.github.jeddict.ai.settings.GenAIProvider.getModelsByProvider;
import io.github.jeddict.ai.util.ColorUtil;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getFontFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getHTMLContent;
import static io.github.jeddict.ai.util.Icons.ICON_ATTACH;
import static io.github.jeddict.ai.util.Icons.ICON_CONTEXT;
import static io.github.jeddict.ai.util.Icons.ICON_COPY;
import static io.github.jeddict.ai.util.Icons.ICON_NEW_CHAT;
import static io.github.jeddict.ai.util.Icons.ICON_NEXT;
import static io.github.jeddict.ai.util.Icons.ICON_PREV;
import static io.github.jeddict.ai.util.Icons.ICON_SAVE;
import static io.github.jeddict.ai.util.Icons.ICON_SEND;
import static io.github.jeddict.ai.util.Icons.ICON_SETTINGS;
import static io.github.jeddict.ai.util.Icons.ICON_STATS;
import static io.github.jeddict.ai.util.Icons.ICON_UPDATE;
import static io.github.jeddict.ai.util.Icons.ICON_WEB;
import io.github.jeddict.ai.util.Labels;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import io.github.jeddict.ai.util.RandomTweetSelector;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.netbeans.api.options.OptionsDisplayer;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Document;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
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
    private JComboBox<String> models;
    private JButton prevButton, nextButton, copyButton, saveButton, openInBrowserButton;
    private AssistantTopComponent topComponent;
    private JEditorPane questionPane;
    private JScrollPane questionScrollPane;
    private final List<Response> responseHistory = new ArrayList<>();
    private int currentResponseIndex = -1;
    private String sourceCode = null;
    private Project project;
    private final List<FileObject> threadContext = new ArrayList<>();
    private final List<FileObject> messageContext = new ArrayList<>();
    private FileObject fileObject;
    private String commitChanges;
    private final PreferencesManager pm = PreferencesManager.getInstance();
    private Tree leaf;

    private Project getProject() {
        if (project != null) {
            return project;
        } else if (threadContext != null && !threadContext.isEmpty()) {
            return FileOwnerQuery.getOwner(threadContext.toArray(FileObject[]::new)[0]);
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
        this.threadContext.addAll(selectedFileObjects);
    }

    public LearnFix(Action action, FileObject selectedFileObject) {
        super(null);
        this.action = action;
        this.threadContext.add(selectedFileObject);
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
                List<FileObject> messageContextCopy = new ArrayList<>(messageContext);
                SwingUtilities.invokeLater(() -> {
                    displayHtmlContent(fileName, name + " AI Assistant");
                    JeddictStreamHandler handler = new JeddictStreamHandler(topComponent) {
                        @Override
                        public void onComplete(String textResponse) {
                            Response response = new Response(null, textResponse, messageContextCopy);
                            sourceCode = EditorUtil.updateEditors(null, topComponent, response, getContextFiles());
                            responseHistory.add(response);
                            currentResponseIndex = responseHistory.size() - 1;
                        }
                    };
                    String response;
                    if (action == Action.TEST) {
                        if (leaf instanceof MethodTree) {
                            response = new JeddictChatModel(handler, getModelName()).generateTestCase(getProject(), null, null, leaf.toString(), null, null);
                        } else {
                            response = new JeddictChatModel(handler, getModelName()).generateTestCase(getProject(), null, treePath.getCompilationUnit().toString(), null, null, null);
                        }
                    } else {
                        if (leaf instanceof MethodTree) {
                            response = new JeddictChatModel(handler, getModelName()).assistJavaMethod(getProject(), leaf.toString());
                        } else {
                            response = new JeddictChatModel(handler, getModelName()).assistJavaClass(getProject(), treePath.getCompilationUnit().toString());
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

    public void askQueryForProjectCommit(Project project, String commitChanges, String intitalCommitMessage) {
        ProjectInformation info = ProjectUtils.getInformation(project);
        String projectName = info.getDisplayName();
        List<FileObject> messageContextCopy = new ArrayList<>(messageContext);
        SwingUtilities.invokeLater(() -> {
            displayHtmlContent(null, projectName + " GenAI Commit");
            JeddictStreamHandler handler = new JeddictStreamHandler(topComponent) {
                @Override
                public void onComplete(String textResponse) {
                    Response response = new Response(null, textResponse, messageContextCopy);
                    sourceCode = EditorUtil.updateEditors(null, topComponent, response, getContextFiles());
                    responseHistory.add(response);
                    currentResponseIndex = responseHistory.size() - 1;
                    LearnFix.this.commitChanges = commitChanges;
                }
            };
            String response = new JeddictChatModel(handler, getModelName()).generateCommitMessageSuggestions(commitChanges, intitalCommitMessage);
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

    private static final String HOME_PAGE = "<div style='margin:20px; padding:20px; border-radius:10px;'>"
            + "<div style='text-align:center;'>"
            + "👋 <strong>Welcome!</strong><br><br>"
            + "I'm here to assist you with any questions you have.<br>"
            + "Feel free to ask anything!<br><br><br><br><br>"
            + "<a href='rules.html' style='text-decoration:none; color:#007bff;'>📘 Learn about context rules and scopes</a><br><br>"
            + "<a href='https://jeddict.github.io/page.html?l=tutorial/AI' style='text-decoration:none; color:#28a745;'>📄 View Documentation</a><br><br>"
            + "<a href='https://github.com/jeddict/jeddict-ai' style='text-decoration:none; color:#ff6600;'>⭐ Like it? Give us a star</a><br><br>"
            + "<a href='tweet' style='text-decoration:none; color:#1DA1F2;'>🐦 Tweet about Jeddict AI</a>"
            + "</div>"
            + "</div>";

    private void initialMessage() {
        JEditorPane init = topComponent.createHtmlPane(HOME_PAGE);
        EventQueue.invokeLater(() -> questionPane.requestFocusInWindow());
        init.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                String link = e.getDescription();
                if ("home.html".equals(link)) {
                    try {
                        String content = getHTMLContent(getHtmlWrapWidth(init), HOME_PAGE);
                        init.setText(content);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if ("rules.html".equals(link)) {
                    try {
                        InputStream inputStream = getClass().getResourceAsStream("/io/github/jeddict/ai/learn/rules.html");
                        if (inputStream != null) {
                            StringBuilder htmlContent = new StringBuilder();
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    htmlContent.append(line).append("\n");
                                }
                            }
                            String content = getHTMLContent(getHtmlWrapWidth(init), htmlContent.toString());
                            init.setText(content);
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }else if ("tweet".equals(link)) {
                    try {
                        java.awt.Desktop.getDesktop().browse(java.net.URI.create(RandomTweetSelector.getRandomTweet()));
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }

    private JPanel filePanel;
    private MessageContextComponentAdapter filePanelAdapter;

    public void addFileTab(FileObject file) {
        if (!messageContext.contains(file)) {
            messageContext.add(file);
            FileTab fileTab = new FileTab(file, filePanel, f -> {
                messageContext.remove(f);
                filePanelAdapter.componentResized(null);
            });
            filePanel.add(fileTab);
            filePanelAdapter.componentResized(null);
            filePanel.revalidate();
            filePanel.repaint();
        }
    }

    public void clearFileTab() {
        messageContext.clear();
        filePanel.removeAll();
        filePanelAdapter.componentResized(null);
        filePanel.revalidate();
        filePanel.repaint();
    }

    private JPanel createBottomPanel(String type, String fileName, Consumer<String> action) {
        JPanel bottomPanel = new JPanel(new BorderLayout());

        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        filePanel = new JPanel();
        filePanelAdapter = new MessageContextComponentAdapter(filePanel);
        filePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        filePanel.setBackground(backgroundColor);
        filePanel.setBorder(BorderFactory.createEmptyBorder());
        filePanel.addComponentListener(filePanelAdapter);

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

        Set<String> modelsByProvider = getModelsByProvider(pm.getProvider()) ;
        modelsByProvider.add(pm.getModelName());
        models = createStyledComboBox(modelsByProvider.toArray(new String[0]));
        models.setSelectedItem(pm.getChatModel() != null ? pm.getChatModel() : pm.getModel());
        models.setToolTipText("AI Models");
        models.addActionListener(e -> {
            String selectedModel = (String) models.getSelectedItem();
            if (selectedModel != null) {
                pm.setChatModel(selectedModel);
            }
        });
        leftButtonPanel.add(models);

        int javaEditorCount = topComponent.getAllCodeEditorCount();

        copyButton = createIconButton(Labels.COPY, ICON_COPY);
        copyButton.setToolTipText("Copy to clipboard");
        copyButton.setVisible(javaEditorCount > 0);
        leftButtonPanel.add(copyButton);

        saveButton = createIconButton(Labels.SAVE, ICON_SAVE);
        saveButton.setToolTipText("Save as");
        saveButton.setVisible(javaEditorCount == 1);
        leftButtonPanel.add(saveButton);

        JButton saveToEditorButton = createIconButton(Labels.UPDATE + " " + fileName, ICON_UPDATE);
        saveToEditorButton.setToolTipText("Update " + fileName);
        saveToEditorButton.setVisible(fileName != null);
        leftButtonPanel.add(saveToEditorButton);

        JButton showChartsButton = createIconButton(Labels.STATS, ICON_STATS);
        showChartsButton.setToolTipText("Show Token Usage Charts");
        rightButtonPanel.add(showChartsButton);
        showChartsButton.addActionListener(e -> {
            TokenUsageChartDialog.showDialog(SwingUtilities.getWindowAncestor(showChartsButton));
        });

        JButton optionsButton = createIconButton(Labels.SETTINGS, ICON_SETTINGS);
        optionsButton.setToolTipText("Open Jeddict AI Assistant Settings");
        optionsButton.addActionListener(e -> OptionsDisplayer.getDefault().open("JeddictAIAssistant"));
        rightButtonPanel.add(optionsButton);

        JButton messageContextButton = createIconButton(Labels.MESSAGE_CONTEXT, ICON_ATTACH);
        messageContextButton.setToolTipText("Attach a file to current message context");
        rightButtonPanel.add(messageContextButton);

        JButton sessionContextButton = createIconButton(Labels.SESSION_CONTEXT, ICON_CONTEXT);
        sessionContextButton.setToolTipText("View detailed context of the current chat session context.");
        sessionContextButton.addActionListener(e -> showFilePathPopup());
        rightButtonPanel.add(sessionContextButton);

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
                updateButton(showChartsButton, showOnlyIcons, ICON_STATS, Labels.STATS + " " + ICON_STATS);
                updateButton(optionsButton, showOnlyIcons, ICON_SETTINGS, Labels.SETTINGS + " " + ICON_SETTINGS);
                updateButton(messageContextButton, showOnlyIcons, ICON_ATTACH, Labels.MESSAGE_CONTEXT + " " + ICON_ATTACH);
                updateButton(sessionContextButton, showOnlyIcons, ICON_CONTEXT, Labels.SESSION_CONTEXT + " " + ICON_CONTEXT);
                updateButton(submitButton, showOnlyIcons, ICON_SEND, Labels.SEND + " " + ICON_SEND);
                updateCombobox(models,  showOnlyIcons);
                topComponent.updateUserPaneButtons(showOnlyIcons);

            }

            private void updateButton(JButton button, boolean iconOnly, String iconText, String fullText) {
                button.setText(iconOnly ? iconText : fullText);
            }
            private void updateCombobox(JComboBox<String> comboBox, boolean iconOnly) {
                comboBox.putClientProperty("minimal", iconOnly);
            }
        };
        buttonPanel.addComponentListener(buttonPanelAdapter);

        questionPane = new JEditorPane();
        questionPane.setEditorKit(createEditorKit("text/x-" + (type == null ? "java" : type)));
        Document doc = questionPane.getDocument();
        doc.putProperty(JEDDICT_EDITOR_CALLBACK, (Consumer<FileObject>) this::addFileTab);

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
            questionPane.setText("");
            clearFileTab();
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
                submitButton.setText("ooo");
                submitButton.setEnabled(false);
                handleQuestion(question, messageContext, submitButton, true);
            }
        };

        submitButton.addActionListener(submitActionListener);

        BiConsumer<String, List<FileObject>> queryUpdate = (newQuery, messageContext) -> {
            handleQuestion(newQuery, messageContext, submitButton, false);
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

        messageContextButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(pm.getLastBrowseDirectory());
            int result = fileChooser.showOpenDialog(bottomPanel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                pm.setLastBrowseDirectory(selectedFile.getParent());
                addFileTab(FileUtil.toFileObject(selectedFile));
            }
        });

        FileTransferHandler.register(bottomPanel, this::addFileTab);
        FileTransferHandler.register(questionPane, this::addFileTab);

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(filePanel);
        bottomPanel.add(Box.createVerticalStrut(0));
        bottomPanel.add(questionScrollPane);
        bottomPanel.add(Box.createVerticalStrut(0));
        bottomPanel.add(buttonPanel);

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
        List<FileObject> fileObjects = new ArrayList<>();
        if (project != null) {
            fileObjects.addAll(getProjectContextList());
        }
        if (threadContext != null) {
            fileObjects.addAll(getFilesContextList(threadContext));
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
        String rules = pm.getSessionRules();
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
            pm.setSessionRules(dialog.getRules());
        }
    }

    private void handleQuestion(String question, List<FileObject> messageContext, JButton submitButton, boolean newQuery) {
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
                List<FileObject> messageContextCopy = new ArrayList<>(messageContext);
                JeddictStreamHandler handler = new JeddictStreamHandler(topComponent) {
                    @Override
                    public void onComplete(String textResponse) {
                        Response response = new Response(question, textResponse, messageContextCopy);
                        if (responseHistory.isEmpty() || !textResponse.equals(responseHistory.get(responseHistory.size() - 1))) {
                            responseHistory.add(response);
                            currentResponseIndex = responseHistory.size() - 1;
                        }
                        SwingUtilities.invokeLater(() -> {
                            BiConsumer<String, List<FileObject>> queryUpdate = (newQuery, messageContext) -> {
                                handleQuestion(newQuery, messageContext, submitButton, false);
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
                    response = new JeddictChatModel(handler, getModelName()).assistDbMetadata(sqlCompletion.getMetaData(), question);
                } else if (commitChanges != null) {
                    response = new JeddictChatModel(handler, getModelName()).generateCommitMessageSuggestions(commitChanges, question);
                } else if (project != null) {
                    response = new JeddictChatModel(handler, getModelName()).generateDescription(getProject(), getProjectContext(getProjectContextList()), null, null, prevChat, question);
                } else if (threadContext != null) {
                    String threadScopeContent = getTextFilesContext(threadContext);
                    List<String> threadScopeImgages = getImageFilesContext(threadContext);
                    String messageScopeContent = getTextFilesContext(messageContext);
                    List<String> messageScopeImgages = getImageFilesContext(messageContext);
                    List<String> images = new ArrayList<>();
                    images.addAll(threadScopeImgages);
                    images.addAll(messageScopeImgages);
                    response = new JeddictChatModel(handler, getModelName()).generateDescription(getProject(), threadScopeContent + '\n' + messageScopeContent, null, images, prevChat, question);
                } else if (treePath == null) {
                    response = new JeddictChatModel(handler, getModelName()).generateDescription(getProject(), null, null, null, prevChat, question);
                } else if (action == Action.TEST) {
                    if (leaf instanceof MethodTree) {
                        response = new JeddictChatModel(handler, getModelName()).generateTestCase(getProject(), null, null, leaf.toString(), prevChat, question);
                    } else {
                        response = new JeddictChatModel(handler, getModelName()).generateTestCase(getProject(), null, treePath.getCompilationUnit().toString(), null, prevChat, question);
                    }
                } else {
                    response = new JeddictChatModel(handler, getModelName()).generateDescription(getProject(), treePath.getCompilationUnit().toString(), treePath.getLeaf() instanceof MethodTree ? treePath.getLeaf().toString() : null, null, prevChat, question);
                }

                if (response != null && !response.isEmpty()) {
                    handler.onComplete(response);
                }

                questionPane.setText("");
                updateHeight();
                clearFileTab();
            } catch (Exception e) {
                e.printStackTrace();
                buttonPanelAdapter.componentResized(null);
                submitButton.setEnabled(true);
            }
        });
    }
    
    private String getModelName() {
        String modelName = (String)models.getSelectedItem();
        if(modelName == null || modelName.isEmpty()) {
            return pm.getModel();
        }
        return modelName;
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
