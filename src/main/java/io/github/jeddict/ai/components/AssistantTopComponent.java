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
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getFontFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getTextColorFromMimeType;
import io.github.jeddict.ai.response.Block;
import io.github.jeddict.ai.util.ColorUtil;
import static io.github.jeddict.ai.util.MimeUtil.JAVA_MIME;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import io.github.jeddict.ai.util.SourceUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.SingleDiffPanel;
import org.openide.util.Exceptions;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
import java.util.function.Consumer;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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

public JTextPane createUserPane(Consumer<String> queryUpdate, String content) {
    JTextPane textPane = new JTextPane();
    textPane.setText(content);
    textPane.setEditable(false);

    // Set fonts/colors like before
    Font newFont = getFontFromMimeType(MIME_PLAIN_TEXT);
    Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
    Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);

    boolean isDark = ColorUtil.isDarkColor(backgroundColor);
    textPane.setBackground(isDark ? backgroundColor.brighter() : ColorUtil.darken(backgroundColor, .05f));
    textPane.setFont(newFont);
    textPane.setForeground(textColor);

    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(textPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
    JButton saveButton = QueryPane.createIconButton("Send ", "\u25B6\uFE0F");
    JButton cancelButton =QueryPane.createIconButton("Cancel", "\u274C"); 

    saveButton.addActionListener(e -> {
        // Handle save (e.g., persist or process content)
        textPane.setEditable(false);
        buttonPanel.setVisible(false);
        
        String question = textPane.getText();
        if (!question.isEmpty()) {
            queryUpdate.accept(question);
        }
    });

    cancelButton.addActionListener(e -> {
        // Handle cancel (e.g., revert content)
        textPane.setEditable(false);
        buttonPanel.setVisible(false);
    });

    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);
    buttonPanel.setVisible(false);  // Initially hidden

    wrapper.add(buttonPanel, BorderLayout.SOUTH);

    // Context menu for edit
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem copyItem = new JMenuItem("Copy");
    JMenuItem editItem = new JMenuItem("Edit Message");

    copyItem.addActionListener(e -> textPane.copy());
    editItem.addActionListener(e -> {
        textPane.setEditable(true);
        buttonPanel.setVisible(true);
        textPane.requestFocus();
    });

    popupMenu.add(copyItem);
    popupMenu.add(editItem);

    textPane.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    });

    parentPanel.add(wrapper);
    return textPane;
}

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

    public int getParseCodeEditor(List<FileObject> fileObjects) {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        Map<JEditorPane, Map<String, String>> editorMethodSignCache = new HashMap<>();
        Map<JEditorPane, Map<String, String>> editorMethodCache = new HashMap<>();
        for (FileObject fileObject : fileObjects) {
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
                                        
                                        CompilationUnit edCu = StaticJavaParser.parse(editorPane.getText() );
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
                                        CompilationUnit edCu = StaticJavaParser.parse(editorPane.getText() );
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
                                    javax.swing.JOptionPane.showMessageDialog(null, "Please select text in the source editor.");
                                }
                            } else {
                                javax.swing.JOptionPane.showMessageDialog(null, "Please select text in the source editor.");
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

    private void diffAction(boolean classSignature, FileObject fileObject, String signature, JEditorPane editorPane, Map<String, String> cachedMethodSignatures) {
        try {
            String origin;
            if (signature.equals(fileObject.getName())) {
                origin = fileObject.asText();
            } else {
                origin = SourceUtil.findMethodSourceInFileObject(fileObject, signature);
            }
            JPanel editorParent = (JPanel) editorPane.getParent();
            JPanel diffPanel = new JPanel();
            diffPanel.setLayout(new BorderLayout());
            FileObject source;
            if (classSignature) {
                source = createTempFileObject(fileObject.getName(), cachedMethodSignatures.get(signature));
            } else {
//                            StreamSource ss1 = StreamSource.createSource(
//                                    "Source " + signature,
//                                    fileObject.getNameExt() + (classSignature ? "" : ("#" + signature)),
//                                    "text/java",
//                                    new StringReader(origin.trim())
//                            );
//                            StreamSource ss2 = StreamSource.createSource(
//                                    "Target " + signature,
//                                    "AI Generated " + signature,
//                                    "text/java",
//                                    new StringReader(cachedMethodSignatures.get(signature))
//                            );
//                            DiffView diffView = Diff.getDefault().createDiff(ss2, ss1);
//                            diffPanel.add(diffView.getComponent(), BorderLayout.CENTER);
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

    private void diffActionWithSelected(String origin, FileObject fileObject, JEditorPane editorPane) {
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

    public FileObject createTempFileObject(String name, String content) throws IOException {
        File tempFile = File.createTempFile("GenAI-" + name, ".java");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        tempFile = FileUtil.normalizeFile(tempFile);
        FileObject fileObject = FileUtil.toFileObject(tempFile);
        return fileObject;
    }

    private String getMethodContentFromSource(FileObject fileObject, String sourceMethodSignature) {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final StringBuilder methodContent = new StringBuilder();

        try {
            javaSource.runUserActionTask(copy -> {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cu = copy.getCompilationUnit();
                Trees trees = copy.getTrees();
                SourcePositions sourcePositions = trees.getSourcePositions();

                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree methodTree, Void v) {
                        String currentSignature = methodTree.getName().toString() + "("
                                + methodTree.getParameters().stream()
                                        .map(param -> param.getType().toString())
                                        .collect(Collectors.joining(",")) + ")";

                        if (currentSignature.equals(sourceMethodSignature)) {
                            long start = sourcePositions.getStartPosition(cu, methodTree);
                            long end = sourcePositions.getEndPosition(cu, methodTree);

                            try {
                                String fullText = copy.getText();
                                methodContent.append(fullText.substring((int) start, (int) end));
                            } catch (Exception e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                        return super.visitMethod(methodTree, v);
                    }
                }.scan(cu, null);
            }, true);
        } catch (IOException e) {
            System.out.println("Error retrieving method " + sourceMethodSignature + " from file " + fileObject.getName() + ": " + e.getMessage());
        }

        return methodContent.toString();
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
