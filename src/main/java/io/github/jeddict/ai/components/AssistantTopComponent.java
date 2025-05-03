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

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import static io.github.jeddict.ai.util.EditorUtil.getExtension;
import static io.github.jeddict.ai.util.EditorUtil.isSuitableForWebAppDirectory;
import static io.github.jeddict.ai.util.StringUtil.convertToCapitalized;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.project.Project;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import static io.github.jeddict.ai.classpath.JeddictQueryCompletionQuery.JEDDICT_EDITOR_CALLBACK;
import static io.github.jeddict.ai.util.EditorUtil.getFontFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getTextColorFromMimeType;
import io.github.jeddict.ai.response.Block;
import io.github.jeddict.ai.util.ColorUtil;
import static io.github.jeddict.ai.util.DiffUtil.diffAction;
import static io.github.jeddict.ai.util.DiffUtil.diffActionWithSelected;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.Icons.ICON_CANCEL;
import static io.github.jeddict.ai.util.Icons.ICON_COPY;
import static io.github.jeddict.ai.util.Icons.ICON_EDIT;
import static io.github.jeddict.ai.util.Icons.ICON_SEND;
import io.github.jeddict.ai.util.Labels;
import static io.github.jeddict.ai.util.MimeUtil.JAVA_MIME;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import io.github.jeddict.ai.util.SourceUtil;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 *
 * @author Shiwani Gupta
 */
public class AssistantTopComponent extends TopComponent {

    public static final ImageIcon icon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/logo16.png"));
    public static final ImageIcon logoIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/logo28.png"));

    public static final String PREFERENCE_KEY = "AssistantTopComponentOpen";
    private final JPanel parentPanel;
    private final Project project;

    private final Map<JEditorPane, JPopupMenu> menus = new HashMap<>();
    private final Map<JEditorPane, List<JMenuItem>> menuItems = new HashMap<>();
    private final Map<JEditorPane, List<JMenuItem>> submenuItems = new HashMap<>();
    private String type = "java";

    public AssistantTopComponent(String name, String type, Project project) {
        setName(name);
        setLayout(new BorderLayout());
        setIcon(icon.getImage());

        this.project = project;
        if (type != null) {
            this.type = type;
        }
        parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));
        add(parentPanel, BorderLayout.CENTER);
    }

    public void clear() {
        parentPanel.removeAll();
        menus.clear();
    }

    private JButton copyButton, editButton, saveButton, cancelButton;
    private JEditorPane queryPane;

    public void updateUserPaneButtons(boolean iconOnly) {
        if (copyButton != null) {
            copyButton.setText(iconOnly ? ICON_COPY : Labels.COPY + " " + ICON_COPY);
            editButton.setText(iconOnly ? ICON_EDIT : Labels.EDIT + " " + ICON_EDIT);
            saveButton.setText(iconOnly ? ICON_SEND : Labels.SAVE + " " + ICON_SEND);
            cancelButton.setText(iconOnly ? ICON_CANCEL : Labels.CANCEL + " " + ICON_CANCEL);
        }
    }

    private void createUserPaneButtons(BiConsumer<String, List<FileObject>> queryUpdate, List<FileObject> messageContext, JPanel buttonPanel) {
        copyButton = QueryPane.createIconButton(Labels.COPY, ICON_COPY);
        editButton = QueryPane.createIconButton(Labels.EDIT, ICON_EDIT);
        saveButton = QueryPane.createIconButton(Labels.SAVE, ICON_SEND);
        cancelButton = QueryPane.createIconButton(Labels.CANCEL, ICON_CANCEL);

        Consumer<Boolean> state = bool -> {
            copyButton.setVisible(bool);
            editButton.setVisible(bool);
            saveButton.setVisible(!bool);
            cancelButton.setVisible(!bool);
        };
        state.accept(true);

        saveButton.addActionListener(e -> {
            queryPane.setEditable(false);
            state.accept(true);

            String question = queryPane.getText();
            if (!question.isEmpty()) {
                queryUpdate.accept(question, messageContext);
            }
        });

        cancelButton.addActionListener(e -> {
            queryPane.setEditable(false);
            state.accept(true);
        });

        buttonPanel.add(copyButton);
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        copyButton.addActionListener(e -> queryPane.copy());
        editButton.addActionListener(e -> {
            queryPane.setEditable(true);
            state.accept(false);
            queryPane.requestFocus();
        });
    }

    public JEditorPane createUserQueryPane(BiConsumer<String, List<FileObject>> queryUpdate, String content, List<FileObject> messageContext) {
        
        Consumer<FileObject> callback = file -> {
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
        };
        
        queryPane = new JEditorPane();
        queryPane.setEditorKit(createEditorKit("text/x-" + (type == null ? "java" : type)));
        queryPane.setText(content);
        queryPane.setEditable(false);
        Document doc = queryPane.getDocument();
        doc.putProperty(JEDDICT_EDITOR_CALLBACK, (Consumer<FileObject>) callback);

        Font newFont = getFontFromMimeType(MIME_PLAIN_TEXT);
        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        boolean isDark = ColorUtil.isDarkColor(backgroundColor);
        queryPane.setBackground(isDark ? backgroundColor.brighter() : ColorUtil.darken(backgroundColor, .05f));
        queryPane.setFont(newFont);
        queryPane.setForeground(textColor);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(queryPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        bottomPanel.setBackground(isDark ? backgroundColor.brighter() : ColorUtil.darken(backgroundColor, .05f));
        filePanel = new JPanel();
        filePanelAdapter = new MessageContextComponentAdapter(filePanel);
        bottomPanel.addComponentListener(filePanelAdapter);
        filePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        filePanel.setBorder(BorderFactory.createEmptyBorder());
        filePanel.setBackground(isDark ? backgroundColor.brighter() : ColorUtil.darken(backgroundColor, .05f));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setBackground(isDark ? backgroundColor.brighter() : ColorUtil.darken(backgroundColor, .05f));
        buttonPanel.setFont(newFont);
        buttonPanel.setForeground(textColor);
        createUserPaneButtons(queryUpdate, messageContext, buttonPanel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.85;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        bottomPanel.add(filePanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.15;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(buttonPanel, gbc);

        wrapper.add(bottomPanel, BorderLayout.SOUTH);

        for (FileObject fileObject : messageContext) {
            FileTab fileTab = new FileTab(fileObject, filePanel, f -> {
                messageContext.remove(f);
                filePanelAdapter.componentResized(null);
            });
            filePanel.add(fileTab);
        }
        filePanelAdapter.componentResized(null);
        filePanel.revalidate();
        filePanel.repaint();
        FileTransferHandler.register(bottomPanel, callback);
        FileTransferHandler.register(queryPane, callback);

        parentPanel.add(wrapper);
        return queryPane;
    }

    private JPanel filePanel;
    private MessageContextComponentAdapter filePanelAdapter;

    public JEditorPane createHtmlPane(String content) {
        JEditorPane editorPane = MarkdownPane.createHtmlPane(content, this);
        parentPanel.add(editorPane);
        return editorPane;
    }

    public JEditorPane createPane() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        Font newFont = getFontFromMimeType(MIME_PLAIN_TEXT);
        java.awt.Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        java.awt.Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);

        editorPane.setFont(newFont);
        editorPane.setForeground(textColor);
        editorPane.setBackground(backgroundColor);
        parentPanel.add(editorPane);
        return editorPane;
    }

    public JEditorPane createCodePane(String mimeType, Block content) {
        JEditorPane editorPane = new JEditorPane();
        EditorKit editorKit = createEditorKit(mimeType == null ? ("text/x-" + type) : mimeType);
        editorPane.setEditorKit(editorKit);
        editorPane.setText(content.getContent());
        editorPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                content.setContent(editorPane.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                content.setContent(editorPane.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                content.setContent(editorPane.getText());
            }
        });
        addContextMenu(editorPane);
        parentPanel.add(editorPane);
        return editorPane;
    }

    public SVGPane createSVGPane(Block content) {
        SVGPane svgPane = new SVGPane();
        JEditorPane sourcePane = svgPane.createPane(content);
        addContextMenu(sourcePane);
        parentPanel.add(svgPane);
        return svgPane;
    }

    public MarkdownPane createMarkdownPane(Block content) {
        MarkdownPane pane = new MarkdownPane();
        JEditorPane sourcePane = pane.createPane(content, this);
        addContextMenu(sourcePane);
        parentPanel.add(pane);
        return pane;
    }

    private void addContextMenu(JEditorPane editorPane) {
        JPopupMenu contextMenu = new JPopupMenu();
        menus.put(editorPane, contextMenu);
        menuItems.clear();
        submenuItems.clear();
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> {
            if (editorPane.getSelectedText() != null) {
                // Copy selected text
                editorPane.copy();
            } else {
                // Select all and copy
                editorPane.selectAll();
                editorPane.copy();
                editorPane.select(0, 0);
            }
        });
        contextMenu.add(copyItem);

        JMenuItem saveAsItem = new JMenuItem("Save As");
        saveAsItem.addActionListener(e -> saveAs(editorPane.getContentType(), editorPane.getText()));
        contextMenu.add(saveAsItem);

        // Add mouse listener to show context menu
        editorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public void saveAs(String mimeType, String content) {

        // Create the file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save As");

        if (mimeType == null || mimeType.equals(JAVA_MIME)) {
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Java Files", "java"));
            String className = extractClassName(content);
            String packageName = extractPackageName(content);
            boolean isTestClass = className != null && className.endsWith("Test");
            String baseDir = isTestClass ? "src/test/java" : "src/main/java";

            if (project != null) {
                String projectRootDir = project.getProjectDirectory().getPath();
                String filePath = Paths.get(projectRootDir, baseDir).toString();
                if (packageName != null) {
                    filePath = Paths.get(filePath, packageName.split("\\.")).toString();
                }
                File targetDir = new File(filePath);
                if (!targetDir.exists()) {
                    boolean dirsCreated = targetDir.mkdirs();
                    if (!dirsCreated) {
                        JOptionPane.showMessageDialog(null, "Failed to create directories: " + targetDir.getAbsolutePath());
                        return;
                    }
                }
                fileChooser.setCurrentDirectory(new File(filePath));
            }
            if (className != null) {
                String defaultFileName = className.endsWith(".java") ? className : className + ".java";
                fileChooser.setSelectedFile(new File(defaultFileName));
            }
        } else {
            if (project != null) {
                String filePath = project.getProjectDirectory().getPath();
                if (isSuitableForWebAppDirectory(mimeType)) {
                    filePath = Paths.get(filePath, "src/main/webapp").toString();
                }
                fileChooser.setCurrentDirectory(new File(filePath));
            }
            String fileExtension = getExtension(mimeType);
            if (fileExtension != null) {
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(convertToCapitalized(fileExtension) + " Files", fileExtension));
            }
        }

        // Show the save dialog
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileExtension = getExtension(mimeType);
            if (!file.getName().endsWith("." + fileExtension)
                    && fileExtension != null) {
                file = new File(file.getAbsolutePath() + "." + fileExtension);
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error saving file: " + ex.getMessage());
            }
        }
    }

    public String extractClassName(String content) {
        String regex = "(?<=\\bclass\\s+)\\w+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(); // Return the class name found
        } else {
            return null; // No class name found
        }
    }

    // Method to extract Java package name using regex
    public String extractPackageName(String content) {
        String regex = "(?<=\\bpackage\\s+)([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*);?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1); // Return the package name found (group 1 without the semicolon)
        } else {
            return null; // No package name found
        }
    }

    public static EditorKit createEditorKit(String mimeType) {
        return MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(EditorKit.class);
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        boolean shouldOpen = prefs.getBoolean(PREFERENCE_KEY, true);
        if (!shouldOpen) {
            this.close();
        }
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.putBoolean(PREFERENCE_KEY, false);
    }

    public JPanel getParentPanel() {
        return parentPanel;
    }

    public String getAllCodeEditorText() {
        StringBuilder allText = new StringBuilder();
        for (int i = 0; i < parentPanel.getComponentCount(); i++) {
            if (parentPanel.getComponent(i) instanceof JEditorPane editorPane) {
                if (!(editorPane.getEditorKit() instanceof javax.swing.text.html.HTMLEditorKit)) {
                    allText.append("\n");
                    allText.append(editorPane.getText());
                    allText.append("\n");
                }
            }
        }
        return allText.toString().trim();
    }

    public String getAllEditorText() {
        StringBuilder allText = new StringBuilder();
        for (int i = 0; i < parentPanel.getComponentCount(); i++) {
            if (parentPanel.getComponent(i) instanceof JEditorPane editorPane) {
                if (!editorPane.getEditorKit().getContentType().equals("text/html")
                        && editorPane.getEditorKit().getContentType().startsWith("text")) {
                    allText.append("<pre><code>");
                    allText.append(editorPane.getText());
                    allText.append("</code></pre>");
                } else {
                    allText.append(editorPane.getText().replaceAll("(?is)<style[^>]*?>.*?</style>", ""));
                }
            }
        }
        return allText.toString().trim();
    }

    public int getAllCodeEditorCount() {
        int count = 0;
        for (int i = 0; i < parentPanel.getComponentCount(); i++) {
            if (parentPanel.getComponent(i) instanceof JEditorPane) {
                JEditorPane editorPane = (JEditorPane) parentPanel.getComponent(i);
                if (!editorPane.getEditorKit().getContentType().equals("text/html")
                        && editorPane.getEditorKit().getContentType().startsWith("text")) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getParseCodeEditor(List<FileObject> context) {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        Map<JEditorPane, Map<String, String>> editorMethodSignCache = new HashMap<>();
        Map<JEditorPane, Map<String, String>> editorMethodCache = new HashMap<>();
        for (FileObject fileObject : context) {
            if (!fileObject.getExt().equalsIgnoreCase("java")) {
                continue;
            }
            try (InputStream stream = fileObject.getInputStream()) {
                String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                CompilationUnit cu = StaticJavaParser.parse(content);
                List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
                Map<String, Integer> fileMethodSignatures = cu.findAll(MethodDeclaration.class).stream()
                        .collect(Collectors.toMap(
                                method -> method.getNameAsString() + "("
                                + method.getParameters().stream()
                                        .map(param -> param.getType().asString())
                                        .collect(Collectors.joining(",")) + ")",
                                method -> method.toString().length()
                        ));
                Map<String, Long> fileMethods = methods.stream()
                        .collect(Collectors.groupingBy(
                                method -> method.getNameAsString(),
                                Collectors.counting()
                        ));

                for (int i = 0; i < parentPanel.getComponentCount(); i++) {
                    if (parentPanel.getComponent(i) instanceof JEditorPane) {
                        JEditorPane editorPane = (JEditorPane) parentPanel.getComponent(i);
                        if (editorPane.getEditorKit().getContentType().equals(JAVA_MIME)) {
                            Set<String> classes = new HashSet<>();
                            Map<String, String> cachedMethodSignatures = editorMethodSignCache.computeIfAbsent(editorPane, ep -> {
                                Map<String, String> methodSignatures = new HashMap<>();
                                try {
                                    MethodDeclaration editorMethod = StaticJavaParser.parseMethodDeclaration(editorPane.getText());
                                    String signature = editorMethod.getNameAsString() + "("
                                            + editorMethod.getParameters().stream()
                                                    .map(param -> param.getType().asString())
                                                    .collect(Collectors.joining(",")) + ")";
                                    methodSignatures.put(signature, editorPane.getText());
                                } catch (Exception e) {
                                    try {

                                        CompilationUnit edCu = StaticJavaParser.parse(editorPane.getText());
                                        edCu.findAll(ClassOrInterfaceDeclaration.class)
                                                .forEach(classDecl -> {
                                                    classes.add(classDecl.getNameAsString());
                                                    methodSignatures.put(classDecl.getNameAsString(), classDecl.toString());
                                                });
//                                        CompilationUnit edCu = StaticJavaParser.parse("class Tmp {" + editorPane.getText() + "}");
                                        List<MethodDeclaration> edMethods = edCu.findAll(MethodDeclaration.class);
                                        for (MethodDeclaration edMethod : edMethods) {
                                            String signature = edMethod.getNameAsString() + "("
                                                    + edMethod.getParameters().stream()
                                                            .map(param -> param.getType().asString())
                                                            .collect(Collectors.joining(",")) + ")";
                                            methodSignatures.put(signature, edMethod.toString());
                                        }
                                    } catch (Exception e1) {
                                        CompilationUnit edCu = StaticJavaParser.parse(editorPane.getText());
                                        edCu.findAll(ClassOrInterfaceDeclaration.class)
                                                .forEach(classDecl -> {
                                                    classes.add(classDecl.getNameAsString());
                                                    methodSignatures.put(classDecl.getNameAsString(), classDecl.toString());
                                                });
                                        if (edCu.getTypes().isNonEmpty()) {
                                            methodSignatures.put(edCu.getType(0).getNameAsString(), edCu.toString());
                                        }
                                    }
                                }
                                return methodSignatures;
                            });

                            Map<String, String> cachedMethods = editorMethodCache.computeIfAbsent(editorPane, ep -> {
                                Map<String, String> methodSignatures = new HashMap<>();
                                try {
                                    MethodDeclaration editorMethod = StaticJavaParser.parseMethodDeclaration(editorPane.getText());
                                    String signature = editorMethod.getNameAsString();
                                    methodSignatures.put(signature, editorPane.getText());
                                } catch (Exception e) {
                                    try {
//                                        CompilationUnit edCu = StaticJavaParser.parse("class Tmp {" + editorPane.getText() + "}");
                                        CompilationUnit edCu = StaticJavaParser.parse(editorPane.getText());
                                        List<MethodDeclaration> edMethods = edCu.findAll(MethodDeclaration.class);
                                        for (MethodDeclaration edMethod : edMethods) {
                                            String signature = edMethod.getNameAsString();
                                            methodSignatures.put(signature, edMethod.toString());
                                        }
                                    } catch (Exception e1) {
                                        CompilationUnit edCu = StaticJavaParser.parse(editorPane.getText());
                                        if (edCu.getTypes().isNonEmpty()) {
                                            methodSignatures.put(edCu.getType(0).getNameAsString(), edCu.toString());
                                        }
                                    }
                                }
                                return methodSignatures;
                            });

                            try {
                                int menuCreationCount = 0;
                                for (Entry<String, Integer> signature : fileMethodSignatures.entrySet()) {
                                    if (createEditorPaneMenus(fileObject, signature.getKey(), signature.getValue(), editorPane, cachedMethodSignatures)) {
                                        menuCreationCount++;
                                    }
                                }
                                if (menuCreationCount == 0) {
                                    for (String method : fileMethods.keySet()) {
                                        if (fileMethods.get(method) == 1) {
                                            if (createEditorPaneMenus(fileObject, method, -1, editorPane, cachedMethods)) {
                                                menuCreationCount++;
                                            }
                                        }
                                    }
                                }

                                createEditorPaneMenus(fileObject, fileObject.getName(), -1, editorPane, cachedMethodSignatures);
                            } catch (Exception e) {
                                System.out.println("Error parsing single method declaration from editor content: " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing file: " + fileObject.getName() + " - " + e.getMessage());
            }
        }

        for (int i = 0; i < parentPanel.getComponentCount(); i++) {
            if (parentPanel.getComponent(i) instanceof JEditorPane editorPane) {
                if (menuItems.get(editorPane) == null) {
                    menuItems.put(editorPane, new ArrayList<>());
                }
                if (editorPane.getEditorKit().getContentType().equals(JAVA_MIME)) {
                    JMenuItem diffMethodItem = new JMenuItem("Diff with Selected Snippet");
                    diffMethodItem.addActionListener(e -> {
                        SwingUtilities.invokeLater(() -> {
                            JTextComponent currenteditor = EditorRegistry.lastFocusedComponent();
                            String currentSelectedText = currenteditor.getSelectedText();
                            final StyledDocument currentDocument = (StyledDocument) currenteditor.getDocument();
                            DataObject currentDO = NbEditorUtilities.getDataObject(currentDocument);
                            if (currentDO != null) {
                                FileObject focusedfile = currentDO.getPrimaryFile();
                                if (focusedfile != null && !currentSelectedText.trim().isEmpty()) {
                                    diffActionWithSelected(currentSelectedText, focusedfile, editorPane);
                                } else {
                                    JOptionPane.showMessageDialog(null, "Please select text in the source editor.");
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Please select text in the source editor.");
                            }
                        });
                    });
                    menuItems.get(editorPane).add(diffMethodItem);
                }
            }
        }

        for (Map.Entry<JEditorPane, List<JMenuItem>> entry : menuItems.entrySet()) {
            JPopupMenu mainMenu = menus.get(entry.getKey());
            if (mainMenu != null) {
                for (JMenuItem jMenuItem : entry.getValue()) {
                    mainMenu.add(jMenuItem);
                }
            }
        }

        for (Map.Entry<JEditorPane, List<JMenuItem>> entry : submenuItems.entrySet()) {
            JMenu methodMenu = new JMenu("Methods");
            for (JMenuItem jMenuItem : entry.getValue()) {
                methodMenu.add(jMenuItem);
            }
            JPopupMenu mainMenu = menus.get(entry.getKey());
            if (mainMenu != null) {
                mainMenu.add(methodMenu);
            }
        }
        return 0;
    }

    private boolean createEditorPaneMenus(FileObject fileObject, String signature, Integer bodyLength, JEditorPane editorPane, Map<String, String> cachedMethodSignatures) {
        boolean classSignature = fileObject.getName().equals(signature);
        if (cachedMethodSignatures.get(signature) != null
                && (cachedMethodSignatures.get(signature).length() != bodyLength || bodyLength == -1)) {
            if (menuItems.get(editorPane) == null) {
                menuItems.put(editorPane, new ArrayList<>());
            }
            if (submenuItems.get(editorPane) == null) {
                submenuItems.put(editorPane, new ArrayList<>());
            }
            String menuSubText = (classSignature ? "" : (signature + " in "));
            JMenuItem updateMethodItem = new JMenuItem("Update " + menuSubText + fileObject.getName());
            updateMethodItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    if (signature.equals(fileObject.getName())) {
                        SourceUtil.updateFullSourceInFile(fileObject, cachedMethodSignatures.get(signature));
                    } else {
                        SourceUtil.updateMethodInSource(fileObject, signature, cachedMethodSignatures.get(signature));
                    }
                });
            });
            if (fileObject.getName().equals(signature)) {
                menuItems.get(editorPane).add(updateMethodItem);
            } else {
                submenuItems.get(editorPane).add(updateMethodItem);
            }

            JMenuItem diffMethodItem = new JMenuItem("Diff " + menuSubText + fileObject.getName());
            diffMethodItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    diffAction(classSignature, fileObject, signature, editorPane, cachedMethodSignatures);
                });
            });
            if (fileObject.getName().equals(signature)) {
                menuItems.get(editorPane).add(diffMethodItem);
            } else {
                submenuItems.get(editorPane).add(diffMethodItem);
            }
            return true;
        }
        return false;
    }

    public int getAllEditorCount() {
        int count = 0;
        for (int i = 0; i < parentPanel.getComponentCount(); i++) {
            if (parentPanel.getComponent(i) instanceof JEditorPane) {
                count++;
            }
        }
        return count;
    }

}
