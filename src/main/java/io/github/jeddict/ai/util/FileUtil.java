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
package io.github.jeddict.ai.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import javax.swing.text.Document;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author Shiwani Gupta
 */
public class FileUtil {

    public static void saveOpenEditor() throws Exception {
        // Get the active TopComponent (the active editor window)
        TopComponent activatedComponent = TopComponent.getRegistry().getActivated();

        // Check if a document is open
        if (activatedComponent != null) {
            // Lookup the DataObject of the active editor
            DataObject dataObject = activatedComponent.getLookup().lookup(DataObject.class);

            if (dataObject != null) {
                // Get the SaveCookie from the DataObject
                SaveCookie saveCookie = dataObject.getLookup().lookup(SaveCookie.class);

                // If there are unsaved changes, save the file
                if (saveCookie != null) {
                    saveCookie.save();
                }
            }
        }
    }

    public static FileObject createTempFileObject(String name, String content) throws IOException {
        File tempFile = File.createTempFile("GenAI-" + name, ".java");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        tempFile = org.openide.filesystems.FileUtil.normalizeFile(tempFile);
        FileObject fileObject = org.openide.filesystems.FileUtil.toFileObject(tempFile);
        return fileObject;
    }

    public static String getLatestContent(FileObject fileObject) {
        try {
            DataObject dataObject = DataObject.find(fileObject);
            EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
            if (editorCookie != null) {
                Document doc = editorCookie.getDocument(); // Do not load if not already open
                if (doc != null) {
                    return doc.getText(0, doc.getLength());
                }
            }
            return fileObject.asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static FileObject getFileObject(Project project, String path) {
        java.io.File file = resolvePath(project, path).toFile();
        return org.openide.filesystems.FileUtil.toFileObject(file);
    }
    
    public static String readContent(Project project, String path) throws IOException {
        return Files.readString(resolvePath(project, path));
    }

    public static Path resolvePath(Project project, String path) {
        Path p = Paths.get(path);
        if (!p.isAbsolute() && project != null) {
            FileObject projectDir = project.getProjectDirectory();
            if (projectDir != null) {
                return Paths.get(org.openide.filesystems.FileUtil.toFile(projectDir).getAbsolutePath(), path)
                        .toAbsolutePath().normalize();
            }
        }
        return p.toAbsolutePath().normalize();
    }
    
    /**
     * Utility method that opens a NetBeans document for a given path and
     * applies the specified action.
     * <p>
     * This centralizes error handling and ensures that the document is saved
     * after the action is applied.
     *
     * @param path the relative or absolute path to the file
     * @param action the action to perform on the opened {@link Document}
     * @param save save the doc after post action
     * @return the result of the action, or an error message
     */
    public static String withDocument(Project project, String path, DocAction action, boolean save) {
        try {
            FileObject fo = getFileObject(project, path);
            if (fo == null) {
                return "File not found: " + path;
            }

            DataObject dobj = DataObject.find(fo);
            EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
            if (cookie == null) {
                return "No editor available for: " + path;
            }

            Document doc = cookie.openDocument();
            String result = action.apply(doc);
            if (save) {
                cookie.saveDocument();
            }
            return result;
        } catch (Exception e) {
            return "Operation failed: " + e.getMessage();
        }
    }

    
    /**
     * Executes an action on a JavaSource obtained from a file in the project.
     *
     * @param project  the NetBeans project
     * @param path     relative path of the Java file
     * @param action   function to run with JavaSource, returns a result
     * @param modify   if true, will open the document in modification mode
     * @param <T>      result type
     * @return the result of the action, or error message if failed
     */
    public static <T> T withJavaSource(Project project, String path,
                                       ThrowingFunction<JavaSource, T> action,
                                       boolean modify) {
        try {
            Path filePath = resolvePath(project, path);
            FileObject fo = org.openide.filesystems.FileUtil.toFileObject(filePath.toFile());
            if (fo == null) {
                return (T) ("File not found: " + path);
            }

            JavaSource javaSource = JavaSource.forFileObject(fo);
            if (javaSource == null) {
                return (T) ("Not a Java source file: " + path);
            }

            return action.apply(javaSource);

        } catch (Exception e) {
            return (T) ("JavaSource operation failed: " + e.getMessage());
        }
    }

}
