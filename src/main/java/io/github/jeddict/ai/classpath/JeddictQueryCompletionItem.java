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
package io.github.jeddict.ai.classpath;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.function.Consumer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

public class JeddictQueryCompletionItem implements CompletionItem {

    private final String text;
    private final int caretOffset;
    private final String prefix;
    private final Consumer<FileObject> callback;

    public JeddictQueryCompletionItem(String text, int caretOffset, String prefix, Consumer<FileObject> callback) {
        this.text = text;
        this.caretOffset = caretOffset;
        this.prefix = prefix;
        this.callback = callback;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        try {
            Document doc = component.getDocument();
            int start = caretOffset - 1;
            while (start >= 0 && doc.getText(start, 1).charAt(0) != '@') {
                start--;
            }
            start++; // move to start of the class name (after '@')
            doc.remove(start - 1, caretOffset - (start - 1)); // remove including '@'
            doc.insertString(start - 1, text, null); // insert class name only
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        Completion.get().hideAll();

        RequestProcessor.getDefault().post(() -> {
            try {
                String className = text;
                FileObject sourceFile = findSourceFile(className);
                if (sourceFile != null) {
                    // Execute the callback with the source file
                    callback.accept(sourceFile);
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        });
    }

    private FileObject findSourceFile(String className) {
        // Convert fully qualified class name to file path (e.g., com.example.MyClass -> com/example/MyClass.java)
        String filePath = className.replace('.', '/') + ".java";

        // Iterate over the open projects to find the source file for the class
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            ClassPath classPath = ClassPath.getClassPath(
                    ProjectUtils.getSources(project).getSourceGroups("java")[0].getRootFolder(),
                    ClassPath.SOURCE
            );

            if (classPath != null) {
                for (ClassPath.Entry entry : classPath.entries()) {
                    FileObject root = entry.getRoot();
                    if (root != null) {
                        // Search for the class file
                        FileObject classFile = findClassFileInDirectory(root, root, filePath);
                        if (classFile != null) {
                            return classFile; // Found the source file
                        }
                    }
                }
            }
        }
        return null; // No file found
    }

    private FileObject findClassFileInDirectory(FileObject root, FileObject dir, String filePath) {
        // Recursively search for the class file
        if (dir.isFolder()) {
            for (FileObject child : dir.getChildren()) {
                FileObject result = findClassFileInDirectory(root, child, filePath);
                if (result != null) {
                    return result;
                }
            }
        } else if ("java".equals(dir.getExt())) {
            // Get the relative path from the source root
            String relativePath = getRelativePath(dir, root);

            // Check if this is the file you're looking for
            if (relativePath.equals(filePath)) {
                return dir; // Found the source file
            }
        }
        return null; // No file found
    }

    private String getRelativePath(FileObject file, FileObject sourceRoot) {
        // Get the full file path
        String fullPath = file.getPath();

        // Get the source root path (source folder from the project)
        String sourceRootPath = sourceRoot.getPath();

        // Ensure that the file path starts after the source root path (removes the source root part)
        if (fullPath.startsWith(sourceRootPath)) {
            // Return the relative path without source root directory
            return fullPath.substring(sourceRootPath.length() + 1).replace(File.separatorChar, '/');
        }
        return null; // Path does not match
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
    }

    @Override
    public int getPreferredWidth(Graphics g, Font font) {
        return CompletionUtilities.getPreferredWidth(text, null, g, font);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        String displayText = text;

        // Apply bold styling to the prefix part
        if (prefix != null && !prefix.isEmpty()) {
            int prefixIndex = text.toLowerCase().indexOf(prefix.toLowerCase());
            if (prefixIndex != -1) {
                // Split text into two parts: before and after the prefix
                String beforePrefix = text.substring(0, prefixIndex);
                String matchingPrefix = text.substring(prefixIndex, prefixIndex + prefix.length());
                String afterPrefix = text.substring(prefixIndex + prefix.length());

                // Create HTML with bold for the prefix
                displayText = beforePrefix + "<b>" + matchingPrefix + "</b>" + afterPrefix;
            }
        }

        // Render the text with HTML formatting
        CompletionUtilities.renderHtml(null, displayText, null, g, defaultFont, defaultColor, width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return 100;
    }

    @Override
    public CharSequence getSortText() {
        return text;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return text;
    }
}
