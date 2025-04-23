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
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Name;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.SingleDiffPanel;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.windows.TopComponent;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;

/**
 *
 * @author Shiwani Gupta
 */
public class AssistantTopComponent extends TopComponent {

    public static final ImageIcon icon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/logo16.png"));
    public static final ImageIcon logoIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/logo28.png"));
    public static final ImageIcon copyIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/copyIcon.gif"));
    public static final ImageIcon saveasIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/saveIcon.png"));
    public static final ImageIcon startIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/startIcon.png"));
    public static final ImageIcon progressIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/progressIcon.png"));
    public static final ImageIcon upIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/browserIcon.png"));
    public static final ImageIcon forwardIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/forwardIcon.png"));
    public static final ImageIcon backIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/backIcon.png"));
    public static final ImageIcon saveToEditorIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/saveToEditorIcon.png"));
    public static final ImageIcon newEditorIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/newEditorIcon.png"));
    public static final ImageIcon attachIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/attachIcon.gif"));
    public static final ImageIcon settingsIcon = new ImageIcon(AssistantTopComponent.class.getResource("/icons/settingsIcon.png"));

    public static final String PREFERENCE_KEY = "AssistantTopComponentOpen";
    private final JPanel parentPanel;
    private HTMLEditorKit htmlEditorKit;
    private final Project project;

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

    public JEditorPane createHtmlPane(String content) {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditorKit(getHTMLEditorKit());
        editorPane.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        editorPane.setEditable(false);
        editorPane.setText(content);
        parentPanel.add(editorPane);
        return editorPane;
    }

    public JEditorPane createPane() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        parentPanel.add(editorPane);
        return editorPane;
    }

    public JEditorPane createCodePane(String content) {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditorKit(createEditorKit("text/x-" + type));
        editorPane.setText(content);
        addContextMenu(editorPane);
        parentPanel.add(editorPane);
        return editorPane;
    }

    public JEditorPane createCodePane(String mimeType, String content) {
        JEditorPane editorPane = new JEditorPane();
        EditorKit editorKit = createEditorKit(mimeType == null ? ("text/x-" + type) : mimeType);
        System.out.println("Mime " + mimeType + " - " + editorKit);
        editorPane.setEditorKit(editorKit);
        editorPane.setText(content);
        addContextMenu(editorPane);
        parentPanel.add(editorPane);
        return editorPane;
    }

    private final Map<JEditorPane, JPopupMenu> menus = new HashMap<>();
    private final Map<JEditorPane, List<JMenuItem>> menuItems = new HashMap<>();

    private void addContextMenu(JEditorPane editorPane) {
        JPopupMenu contextMenu = new JPopupMenu();
        menus.put(editorPane, contextMenu);
        menuItems.clear();
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

        if (mimeType == null || mimeType.equals("text/x-java")) {
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
            if (!file.getName().endsWith("." + fileExtension)) {
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
                    allText.append(editorPane.getText());
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
                Set<String> fileMethodSignatures = methods.stream()
                        .map(method -> method.getNameAsString() + "("
                        + method.getParameters().stream()
                                .map(param -> param.getType().asString())
                                .collect(Collectors.joining(",")) + ")")
                        .collect(Collectors.toSet());
                Map<String, Long> fileMethods = methods.stream()
                        .collect(Collectors.groupingBy(
                                method -> method.getNameAsString(),
                                Collectors.counting()
                        ));

                for (int i = 0; i < parentPanel.getComponentCount(); i++) {
                    if (parentPanel.getComponent(i) instanceof JEditorPane) {
                        JEditorPane editorPane = (JEditorPane) parentPanel.getComponent(i);
                        if (editorPane.getEditorKit().getContentType().equals("text/x-java")) {

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
                                        CompilationUnit edCu = StaticJavaParser.parse("class Tmp {" + editorPane.getText() + "}");
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
                                        CompilationUnit edCu = StaticJavaParser.parse("class Tmp {" + editorPane.getText() + "}");
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
                                for (String signature : fileMethodSignatures) {
                                    if (createEditorPaneMenus(fileObject, signature, editorPane, cachedMethodSignatures)) {
                                        menuCreationCount++;
                                    }
                                }
                                if (menuCreationCount == 0) {
                                    for (String method : fileMethods.keySet()) {
                                        if (fileMethods.get(method) == 1) {
                                            if (createEditorPaneMenus(fileObject, method, editorPane, cachedMethods)) {
                                                menuCreationCount++;
                                            }
                                        }
                                    }
                                }
                                if (menuCreationCount == 0) {
                                    createEditorPaneMenus(fileObject, fileObject.getName(), editorPane, cachedMethodSignatures);
                                }
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
                if (editorPane.getEditorKit().getContentType().equals("text/x-java")) {
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
            if (entry.getValue().size() > 3) {
                JMenu methodMenu = new JMenu("Methods");
                for (JMenuItem jMenuItem : entry.getValue()) {
                    methodMenu.add(jMenuItem);
                }
                menus.get(entry.getKey()).add(methodMenu);
            } else {
                JPopupMenu mainMenu = menus.get(entry.getKey());
                for (JMenuItem jMenuItem : entry.getValue()) {
                    mainMenu.add(jMenuItem);
                }
            }
        }
        return 0;
    }

    private boolean createEditorPaneMenus(FileObject fileObject, String signature, JEditorPane editorPane, Map<String, String> cachedMethodSignatures) {
        boolean classSignature = fileObject.getName().equals(signature);
        if (cachedMethodSignatures.get(signature) != null) {
            if (menuItems.get(editorPane) == null) {
                menuItems.put(editorPane, new ArrayList<>());
            }
            String menuSubText = (classSignature ? "" : (signature + " in "));
            JMenuItem updateMethodItem = new JMenuItem("Update " + menuSubText + fileObject.getName());
            updateMethodItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    if (signature.equals(fileObject.getName())) {
                        updateFullSourceInFile(fileObject, cachedMethodSignatures.get(signature));
                    } else {
                        updateMethodInSource(fileObject, signature, cachedMethodSignatures.get(signature));
                    }
                });
            });
            menuItems.get(editorPane).add(updateMethodItem);

            JMenuItem diffMethodItem = new JMenuItem("Diff " + menuSubText + fileObject.getName());
            diffMethodItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    diffAction(classSignature, fileObject, signature, editorPane, cachedMethodSignatures);
                });
            });
            menuItems.get(editorPane).add(diffMethodItem);
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
                origin = findMethodSourceInFileObject(fileObject, signature);
            }
            JPanel editorParent = (JPanel) editorPane.getParent();
            JPanel diffPanel = new JPanel();
            diffPanel.setLayout(new BorderLayout());
            if (classSignature) {
                FileObject source = createTempFileObject(fileObject.getName(), cachedMethodSignatures.get(signature));
                SingleDiffPanel sdp = new SingleDiffPanel(source, fileObject, null);
                diffPanel.add(sdp, BorderLayout.CENTER);
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
                FileObject source = createTempFileObject(fileObject.getName(), fileObject.asText());
                MethodUpdater.updateMethod(source, signature, cachedMethodSignatures.get(signature));
                SingleDiffPanel sdp = new SingleDiffPanel(source, fileObject, null);
                diffPanel.add(sdp, BorderLayout.CENTER);
            }

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

    private void updateMethodInSource(FileObject fileObject, String sourceMethodSignature, String methodContent) {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        try {
            javaSource.runModificationTask(copy -> {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree methodTree, Void v) {
                        Name name = methodTree.getName();
                        String targetMethodSignature = name.toString() + "("
                                + methodTree.getParameters().stream()
                                        .map(param -> param.getType().toString())
                                        .collect(Collectors.joining(",")) + ")";

                        // Compare the signature with the method being updated
                        if (targetMethodSignature.equals(sourceMethodSignature)) {
                            long startPos = copy.getTrees().getSourcePositions().getStartPosition(copy.getCompilationUnit(), methodTree);
                            long endPos = copy.getTrees().getSourcePositions().getEndPosition(copy.getCompilationUnit(), methodTree);

                            try {
                                if (copy.getDocument() == null) {
                                    openFileInEditor(fileObject);
                                }
                                insertAndReformat(copy.getDocument(), methodContent, (int) startPos, (int) endPos - (int) startPos);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        return super.visitMethod(methodTree, v);
                    }
                }.scan(copy.getCompilationUnit(), null);

            }).commit();
        } catch (IOException e) {
            System.out.println("Error updating method " + sourceMethodSignature + " in file " + fileObject.getName() + ": " + e.getMessage());
        }
    }

    private void updateFullSourceInFile(FileObject fileObject, String newSourceContent) {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        try {
            javaSource.runModificationTask(copy -> {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                Document document = copy.getDocument();

                try {
                    // Open the file in the editor if it is not already open
                    if (document == null) {
                        openFileInEditor(fileObject);
                        document = copy.getDocument(); // Re-fetch the document after opening
                    }

                    // Replace the entire document content with the new source content
                    document.remove(0, document.getLength());
                    document.insertString(0, newSourceContent, null);

                } catch (BadLocationException | IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }).commit();
        } catch (IOException e) {
            System.out.println("Error updating source in file " + fileObject.getName() + ": " + e.getMessage());
        }
    }

    private String findMethodSourceInFileObject(FileObject fileObject, String sourceMethodSignature) {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        StringBuilder methodSource = new StringBuilder();

        try {
            javaSource.runModificationTask(copy -> {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree methodTree, Void v) {
                        Name name = methodTree.getName();
                        String targetMethodSignature = name.toString() + "("
                                + methodTree.getParameters().stream()
                                        .map(param -> param.getType().toString())
                                        .collect(Collectors.joining(",")) + ")";

                        // Check if the signatures match
                        if (targetMethodSignature.equals(sourceMethodSignature)) {
                            // Construct the method source code
                            methodSource.append(methodTree.toString());
                        }
                        return super.visitMethod(methodTree, v);
                    }
                }.scan(copy.getCompilationUnit(), null);
            }).commit();
        } catch (IOException e) {
            System.out.println("Error finding method " + sourceMethodSignature + " in file " + fileObject.getName() + ": " + e.getMessage());
        }

        if (methodSource.isEmpty()) {
            try {
                javaSource.runModificationTask(copy -> {
                    copy.toPhase(JavaSource.Phase.RESOLVED);
                    new TreePathScanner<Void, Void>() {
                        @Override
                        public Void visitMethod(MethodTree methodTree, Void v) {
                            Name name = methodTree.getName();
                            String targetMethodSignature = name.toString();

                            // Check if the signatures match
                            if (targetMethodSignature.equals(sourceMethodSignature)) {
                                // Construct the method source code
                                methodSource.append(methodTree.toString());
                            }
                            return super.visitMethod(methodTree, v);
                        }
                    }.scan(copy.getCompilationUnit(), null);
                }).commit();
            } catch (IOException e) {
                System.out.println("Error finding method " + sourceMethodSignature + " in file " + fileObject.getName() + ": " + e.getMessage());
            }
        }
        return methodSource.toString(); // Return the method source code
    }

    public void openFileInEditor(FileObject fileObject) {
        try {
            // Get the DataObject associated with the FileObject
            DataObject dataObject = DataObject.find(fileObject);

            // Lookup for the EditorCookie from the DataObject
            EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);

            if (editorCookie != null) {
                // Open the file in the editor
                editorCookie.open();
                StatusDisplayer.getDefault().setStatusText("File opened in editor: " + fileObject.getNameExt());
            } else {
                StatusDisplayer.getDefault().setStatusText("Failed to find EditorCookie for file: " + fileObject.getNameExt());
            }
        } catch (DataObjectNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void insertAndReformat(Document document, String content, int startPosition, int lengthToRemove) {
        try {
            if (lengthToRemove > 0) {
                document.remove(startPosition, lengthToRemove);
            }
            document.insertString(startPosition, content, null);
            Reformat reformat = Reformat.get(document);
            reformat.lock();
            try {
                reformat.reformat(startPosition, startPosition + content.length());
            } finally {
                reformat.unlock();
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
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

    private HTMLEditorKit getHTMLEditorKit() {
        if (htmlEditorKit != null) {
            return htmlEditorKit;
        }
        htmlEditorKit = new HTMLEditorKit();
        StyleSheet styleSheet = htmlEditorKit.getStyleSheet();

        try (InputStream is = getClass().getResourceAsStream("html-styles.css")) {
            if (is != null) {
                String css = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                styleSheet.addRule(css);
            } else {
                System.err.println("CSS file not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return htmlEditorKit;
    }

}
