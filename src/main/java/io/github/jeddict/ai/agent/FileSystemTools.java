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
import io.github.jeddict.ai.lang.JeddictStreamHandler;
import io.github.jeddict.ai.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Element;

import org.netbeans.api.project.Project;

/**
 * Collection of tools that expose file system and editor operations inside
 * Apache NetBeans to an AI assistant.
 * <p>
 * These tools allow language models to read, modify, and manage project files
 * in a controlled and safe way.
 */
public class FileSystemTools {

    private final Project project;
    private final JeddictStreamHandler handler;

    /**
     * Creates a new {@code FileSystemTools } instance bound to the given
     * project.
     *
     * @param project the NetBeans project in which these tools operate
     * @param handler an optional stream handler for reporting actions
     */
    public FileSystemTools(Project project, JeddictStreamHandler handler) {
        this.project = project;
        this.handler = handler;
    }
    
    /**
     * Reads the raw content of a file on disk.
     *
     * @param path the file path relative to the project
     * @return the file content, or an error message if it could not be read
     */
    @Tool("Read the content of a file by path")
   public String readFile(String path) {
        log("📖 Reading file from disk: ", path);
        try {
            String content = FileUtil.readContent(project, path);
            return content;
        } catch (IOException e) {
            log("❌ Failed to read file: " + e.getMessage() + " in file: ", path);
            return "Could not read file: " + e.getMessage();
        }
    }

    /**
     * Searches for a regular expression inside a file.
     *
     * @param path the file path relative to the project
     * @param pattern the regex pattern to search for
     * @return all matches with their offsets, or a message if none were found
     */
    @Tool("Search for a regex pattern in a file by path")
    public String searchInFile(String path, String pattern) {
        log("🔎 Looking for '" + pattern + "' inside", path);
        try {
            String content = FileUtil.readContent(project, path);
            Matcher m = Pattern.compile(pattern).matcher(content);
            StringBuilder result = new StringBuilder();
            while (m.find()) {
                result.append("Match at ").append(m.start())
                        .append(": ").append(m.group()).append("\n");
            }
            return result.length() > 0 ? result.toString() : "No matches found.";
        } catch (IOException e) {
            return "Search failed: " + e.getMessage();
        }
    }

    /**
     * Find a line number to insert code after the end of a Java method or
     * constructor. This method heuristically scans the file content lines to
     * find the closing brace of the method. It accounts for Javadoc and nested
     * braces for accurate placement.
     *
     * @param fileContent the full Java source code as a String
     * @param methodName the method or constructor name to find
     * @return the line number after the method ends, or -1 if not found
     */
    private int findInsertionLineAfterMethod(String fileContent, String methodName) {
        String[] lines = fileContent.split("\r?\n");
        int methodStartLine = -1;
        int braceDepth = 0;
        boolean inMethod = false;

        Pattern methodPattern = Pattern.compile("\\b" + Pattern.quote(methodName) + "\\s*\\(.*\\)\\s*\\{\\s*$");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!inMethod) {
                // Detect method declaration line
                if (methodPattern.matcher(line).find()) {
                    inMethod = true;
                    methodStartLine = i;
                    // Count opening brace
                    braceDepth = 1;
                }
            } else {
                // Inside method, track braces to find method end
                braceDepth += countChar(line, '{');
                braceDepth -= countChar(line, '}');
                if (braceDepth == 0) {
                    // Method ends here
                    return i + 1; // return line after method end
                }
            }
        }
        return -1;
    }

    /**
     * Inserts a line after the end of a specified Java method or constructor in
     * the file. Uses findInsertionLineAfterMethod to get a robust insertion
     * point.
     *
     * @param path the file path relative to the project
     * @param methodName the method or constructor name where to insert after
     * @param lineText the text to insert as a new line
     * @return status message
     */
    @Tool("Insert a line of text immediately after the specified method in the given file")
    public String insertLineAfterMethod(String path, String methodName, String lineText) {
        log("✏️ Inserting line after method '" + methodName + "' in file: ", path);
        String content = FileUtil.withDocument(project, path, doc -> doc.getText(0, doc.getLength()), false);
        if (content.startsWith("Could not")) {
            log("❌ Failed to read file: ", path);
            return "Failed to read file: " + content;
        }
        int insertLine = findInsertionLineAfterMethod(content, methodName);
        if (insertLine < 0) {
            log("⚠️ Method not found: " + methodName + " in file ", path);
            return "Method not found: " + methodName;
        }
        log("✅ Inserting text at line " + insertLine + " in file: ", path);
        return insertLineInFile(path, insertLine, lineText);
    }

    /**
     * Counts occurrences of a character in a string.
     *
     * @param line the string to search
     * @param ch the character to count
     * @return count of characters found
     */
    private int countChar(String line, char ch) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ch) {
                count++;
            }
        }
        return count;
    }

    /**
     * Replaces parts of a file content matching a regex pattern with
     * replacement text.
     *
     * @param path the file path relative to the project
     * @param regexPattern the regex pattern to search for
     * @param replacement the replacement text
     * @return a status message
     */
    @Tool("Replace parts of a file content by regex pattern with replacement text")
    public String replaceSnippetByRegex(String path, String regexPattern, String replacement) {
        log("🔄 Replacing text matching regex '" + regexPattern + "' in file: ", path);

        return FileUtil.withDocument(project, path, doc -> {
            try {
                String original = doc.getText(0, doc.getLength());
                String modified = original.replaceAll(regexPattern, replacement);

                if (original.equals(modified)) {
                    log("⚠️ No matches found for regex '" + regexPattern + "' in file: ", path);
                    return "No matches found for pattern.";
                }

                doc.remove(0, doc.getLength());
                doc.insertString(0, modified, null);

                log("✅ Replacement completed in file: ", path);
                return "File snippet replaced successfully.";
            } catch (Exception e) {
                log("❌ Replacement failed " + e.getMessage() + " in file: ", path);
                return "Replacement failed: " + e.getMessage();
            }
        }, true);
    }

    /**
     * Replaces the full content of a file with the given text.
     *
     * @param path the file path relative to the project
     * @param newContent the new content to write
     * @return a status message
     */
    @Tool("Replace the full content of a file by path with new text")
    public String replaceFileContent(String path, String newContent) {
        log("📝 Replacing entire content of file: ", path);

        return FileUtil.withDocument(project, path, doc -> {
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, newContent, null);

                log("✅ File content replaced successfully: ", path);
                return "File updated";
            } catch (Exception e) {
                log("❌ Failed to replace content " + e.getMessage() + " in file: ", path);
                return "Update failed: " + e.getMessage();
            }
        }, true);
    }

    /**
     * Inserts a line at the given line number (0-based).
     *
     * @param path the file path relative to the project
     * @param lineNumber the line number (0-based)
     * @param lineText the text of the new line
     * @return a status message
     */
    @Tool("Insert a line of code at a given line number (0-based) in a file by path")
    public String insertLineInFile(String path, int lineNumber, String lineText) {
        log("✏️ Inserting line at " + lineNumber + " in file: ", path);

        return FileUtil.withDocument(project, path, doc -> {
            try {
                Element root = doc.getDefaultRootElement();
                if (lineNumber < 0 || lineNumber > root.getElementCount()) {
                    log("⚠️ Invalid line number " + lineNumber + " for file: ", path);
                    return "Invalid line number: " + lineNumber;
                }

                int offset = (lineNumber == root.getElementCount())
                        ? doc.getLength()
                        : root.getElement(lineNumber).getStartOffset();

                doc.insertString(offset, lineText + System.lineSeparator(), null);

                log("✅ Inserted line at " + lineNumber + " in file: ", path);
                return "Inserted line at " + lineNumber;
            } catch (Exception e) {
                log("❌ Line insert failed: " + e.getMessage() + " in file: ", path);
                return "Line insert failed: " + e.getMessage();
            }
        }, true);
    }

    /**
     * Creates a new file at the given path.
     *
     * @param path the file path relative to the project
     * @param content optional content to write into the file
     * @return a status message
     */
    @Tool("Create a new file at the given path with optional content")
    public String createFile(String path, String content) {
        log("📄 Creating new file: ", path);
        try {
            Path filePath = FileUtil.resolvePath(project, path);
            if (Files.exists(filePath)) {
                log("⚠️ File already exists: ", path);
                return "File already exists: " + path;
            }

            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content != null ? content : "");

            log("✅ File created successfully: ", path);
            return "File created";
        } catch (IOException e) {
            log("❌ File creation failed: " + e.getMessage() + " in file: ", path);
            return "File creation failed: " + e.getMessage();
        }
    }

    /**
     * Deletes a file.
     *
     * @param path the file path relative to the project
     * @return a status message
     */
    @Tool("Delete a file at the given path")
    public String deleteFile(String path) {
        log("🗑️ Attempting to delete file: ", path);
        try {
            Path filePath = FileUtil.resolvePath(project, path);
            if (!Files.exists(filePath)) {
                log("⚠️ File not found: ", path);
                return "File not found: " + path;
            }

            Files.delete(filePath);
            log("✅ File deleted successfully: ", path);
            return "File deleted";
        } catch (IOException e) {
            log("❌ File deletion failed: " + e.getMessage() + " in file: ", path);
            return "File delete failed: " + e.getMessage();
        }
    }

    /**
     * Lists all files and directories in a directory.
     *
     * @param path the directory path relative to the project
     * @return a list of files and directories, or an error message
     */
    @Tool("List all files and directories inside a given directory path")
    public String listFilesInDirectory(String path) {
        log("📂 Listing contents of directory: ", path);
        try {
            Path dirPath = FileUtil.resolvePath(project, path);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                log("⚠️ Directory not found: ", path);
                return "Directory not found: " + path;
            }

            StringBuilder result = new StringBuilder(dirPath.getFileName() + ":\n");
            Files.list(dirPath).forEach(p -> {
                result.append(" - ").append(p.getFileName())
                        .append(Files.isDirectory(p) ? "/" : "")
                        .append("\n");
            });

            log("✅ Directory listed successfully: ", path);
            return result.toString();
        } catch (IOException e) {
            log("❌ Failed to list directory: " + e.getMessage() + " in ", path);
            return "Could not list directory: " + e.getMessage();
        }
    }

    /**
     * Creates a new directory.
     *
     * @param path the directory path relative to the project
     * @return a status message
     */
    @Tool("Create a new directory at the given path")
    public String createDirectory(String path) {
        log("📂 Creating new directory: ", path);
        try {
            Path dirPath = FileUtil.resolvePath(project, path);
            if (Files.exists(dirPath)) {
                log("⚠️ Directory already exists: ", path);
                return "Directory already exists: " + path;
            }

            Files.createDirectories(dirPath);
            log("✅ Directory created successfully: ", path);
            return "Directory created";
        } catch (IOException e) {
            log("❌ Directory creation failed: " + e.getMessage() + " in ", path);
            return "Directory creation failed: " + e.getMessage();
        }
    }

    /**
     * Deletes a directory (must be empty).
     *
     * @param path the directory path relative to the project
     * @return a status message
     */
    @Tool("Delete a directory at the given path (must be empty)")
    public String deleteDirectory(String path) {
        log("🗑️ Attempting to delete directory: ", path);
        try {
            Path dirPath = FileUtil.resolvePath(project, path);
            if (!Files.exists(dirPath)) {
                log("⚠️ Directory not found: ", path);
                return "Directory not found: " + path;
            }
            if (!Files.isDirectory(dirPath)) {
                log("⚠️ Not a directory: ", path);
                return "Not a directory: " + path;
            }

            Files.delete(dirPath);
            log("✅ Directory deleted successfully: ", path);
            return "Directory deleted";
        } catch (IOException e) {
            log("❌ Directory deletion failed: " + e.getMessage() + " in ", path);
            return "Directory delete failed: " + e.getMessage();
        }
    }

    /**
     * Logs the current action to the {@link JeddictStreamHandler}, if
     * available.
     *
     * @param action the action being performed (e.g., "Reading", "Updating")
     * @param path the file path on which the action is performed
     */
    private void log(String action, String path) {
        if (handler != null && path != null) {
            String fileName = java.nio.file.Paths.get(path).getFileName().toString();
            handler.onToolingResponse(action + " " + fileName + "\n");
        }
    }
}
