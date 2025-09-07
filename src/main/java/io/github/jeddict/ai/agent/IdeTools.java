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
package io.github.jeddict.ai.agent;

import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.project.Project;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Tools available for the AI assistant inside Apache NetBeans. These allow LLMs
 * to interact with the IDE editor safely.
 *
 * @author Gaurav Gupta
 */
public class IdeTools {

    private final Project project;

    public IdeTools(Project project) {
        this.project = project;
    }

    // ----------------------------
    // Utility: Resolve paths
    // ----------------------------
    private Path resolvePath(String path) {
        Path p = Paths.get(path);
        if (!p.isAbsolute() && project != null) {
            FileObject projectDir = project.getProjectDirectory();
            if (projectDir != null) {
                return Paths.get(FileUtil.toFile(projectDir).getAbsolutePath(), path)
                        .toAbsolutePath().normalize();
            }
        }
        return p.toAbsolutePath().normalize();
    }

    // ----------------------------
    // Utility: Resolve active source editor
    // ----------------------------
    private EditorCookie getActiveEditorCookie() {
        TopComponent active = WindowManager.getDefault().getRegistry().getActivated();
        if (active != null) {
            DataObject dobj = active.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
                if (cookie != null) {
                    return cookie;
                }
            }
        }

        TopComponent editor = TopComponent.getRegistry().getOpened().stream()
                .filter(tc -> tc.getLookup().lookup(EditorCookie.class) != null)
                .findFirst()
                .orElse(null);

        if (editor != null) {
            return editor.getLookup().lookup(EditorCookie.class);
        }

        return null;
    }

    private Document getActiveDocument() {
        EditorCookie cookie = getActiveEditorCookie();
        return cookie != null ? cookie.getDocument() : null;
    }

    // ----------------------------
    // Helper: Normalize and get FileObject
    // ----------------------------
    private FileObject getFileObject(String path) {
        java.io.File file = resolvePath(path).toFile();
        return FileUtil.toFileObject(file);
    }

    // ----------------------------
    // Basic File System Tools
    // ----------------------------
    @Tool("Read the content of a file by path")
    public String readFile(String path) {
        try {
            return Files.readString(resolvePath(path));
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool("Search for a regex pattern in a file by path")
    public String searchInFile(String path, String pattern) {
        try {
            String content = Files.readString(resolvePath(path));
            Matcher m = Pattern.compile(pattern).matcher(content);
            StringBuilder result = new StringBuilder();
            while (m.find()) {
                result.append("Match at ").append(m.start())
                        .append(": ").append(m.group()).append("\n");
            }
            return result.toString().isEmpty() ? "No matches found" : result.toString();
        } catch (IOException e) {
            return "Error searching file: " + e.getMessage();
        }
    }

    // ----------------------------
    // NetBeans Editor Tools
    // ----------------------------
    @Tool("Read the text content of a file in NetBeans by path")
    public String readFileContent(String path) {
        try {
            FileObject fo = getFileObject(path);
            if (fo == null) {
                return "File not found: " + path;
            }

            DataObject dobj = DataObject.find(fo);
            EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
            if (cookie == null) {
                return "No editor available for file: " + path;
            }

            Document doc = cookie.openDocument();
            return doc.getText(0, doc.getLength());
        } catch (Exception e) {
            return "Error reading file " + path + ": " + e.getMessage();
        }
    }

    @Tool("Replace the full content of a file by path with new text")
    public String replaceFileContent(String path, String newContent) {
        try {
            FileObject fo = getFileObject(path);
            if (fo == null) {
                return "File not found: " + path;
            }

            DataObject dobj = DataObject.find(fo);
            EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
            if (cookie == null) {
                return "No editor available for file: " + path;
            }

            Document doc = cookie.openDocument();
            doc.remove(0, doc.getLength());
            doc.insertString(0, newContent, null);
            return "Replaced file content in: " + path;
        } catch (Exception e) {
            return "Error replacing content in " + path + ": " + e.getMessage();
        }
    }

    @Tool("Insert text at a given offset in a file by path")
    public String insertTextInFile(String path, int offset, String newText) {
        try {
            FileObject fo = getFileObject(path);
            if (fo == null) {
                return "File not found: " + path;
            }

            DataObject dobj = DataObject.find(fo);
            EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
            if (cookie == null) {
                return "No editor available for file: " + path;
            }

            Document doc = cookie.openDocument();
            doc.insertString(offset, newText, null);
            return "Inserted text at offset " + offset + " in file: " + path;
        } catch (Exception e) {
            return "Error inserting text in " + path + ": " + e.getMessage();
        }
    }

    @Tool("Insert a line of code at a given line number (0-based) in a file by path")
    public String insertLineInFile(String path, int lineNumber, String lineText) {
        try {
            FileObject fo = getFileObject(path);
            if (fo == null) {
                return "File not found: " + path;
            }

            DataObject dobj = DataObject.find(fo);
            EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
            if (cookie == null) {
                return "No editor available for file: " + path;
            }

            Document doc = cookie.openDocument();
            Element root = doc.getDefaultRootElement();
            if (lineNumber < 0 || lineNumber > root.getElementCount()) {
                return "Invalid line number: " + lineNumber;
            }

            int offset = (lineNumber == root.getElementCount())
                    ? doc.getLength()
                    : root.getElement(lineNumber).getStartOffset();

            doc.insertString(offset, lineText + System.lineSeparator(), null);
            return "Inserted line at " + lineNumber + " in file: " + path;
        } catch (Exception e) {
            return "Error inserting line in " + path + ": " + e.getMessage();
        }
    }

    @Tool("Get the number of lines in a file by path")
    public String countLinesInFile(String path) {
        try {
            FileObject fo = getFileObject(path);
            if (fo == null) {
                return "File not found: " + path;
            }

            DataObject dobj = DataObject.find(fo);
            EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
            if (cookie == null) {
                return "No editor available for file: " + path;
            }

            Document doc = cookie.openDocument();
            return "File " + path + " contains " + doc.getDefaultRootElement().getElementCount() + " lines.";
        } catch (Exception e) {
            return "Error counting lines in " + path + ": " + e.getMessage();
        }
    }

    // ----------------------------
    // File System Utilities
    // ----------------------------
    @Tool("Create a new file at the given path with optional content")
    public String createFile(String path, String content) {
        try {
            Path filePath = resolvePath(path);
            if (Files.exists(filePath)) {
                return "File already exists: " + path;
            }
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content != null ? content : "");
            return "Created file at: " + path;
        } catch (IOException e) {
            return "Error creating file " + path + ": " + e.getMessage();
        }
    }

    @Tool("Delete a file at the given path")
    public String deleteFile(String path) {
        try {
            Path filePath = resolvePath(path);
            if (!Files.exists(filePath)) {
                return "File not found: " + path;
            }
            Files.delete(filePath);
            return "Deleted file: " + path;
        } catch (IOException e) {
            return "Error deleting file " + path + ": " + e.getMessage();
        }
    }

    @Tool("List all files and directories inside a given directory path")
    public String listFilesInDirectory(String path) {
        try {
            Path dirPath = resolvePath(path);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return "Directory not found: " + path;
            }

            StringBuilder result = new StringBuilder("Contents of " + path + ":\n");
            Files.list(dirPath).forEach(p -> {
                result.append(p.getFileName())
                        .append(Files.isDirectory(p) ? "/" : "")
                        .append("\n");
            });

            return result.toString();
        } catch (IOException e) {
            return "Error listing directory " + path + ": " + e.getMessage();
        }
    }

    @Tool("Create a new directory at the given path")
    public String createDirectory(String path) {
        try {
            Path dirPath = resolvePath(path);
            if (Files.exists(dirPath)) {
                return "Directory already exists: " + path;
            }
            Files.createDirectories(dirPath);
            return "Created directory at: " + path;
        } catch (IOException e) {
            return "Error creating directory " + path + ": " + e.getMessage();
        }
    }

    @Tool("Delete a directory at the given path (must be empty)")
    public String deleteDirectory(String path) {
        try {
            Path dirPath = resolvePath(path);
            if (!Files.exists(dirPath)) {
                return "Directory not found: " + path;
            }
            if (!Files.isDirectory(dirPath)) {
                return "Path is not a directory: " + path;
            }
            Files.delete(dirPath); // Only works if directory is empty
            return "Deleted directory: " + path;
        } catch (IOException e) {
            return "Error deleting directory " + path + ": " + e.getMessage();
        }
    }
}
