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

import io.github.jeddict.ai.settings.PreferencesManager;
import static io.github.jeddict.ai.util.SourceUtil.removeJavadoc;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Gaurav Gupta
 */
public class ContextHelper {

    private final static PreferencesManager pm = PreferencesManager.getInstance();

    public static String getProjectContext(List<FileObject> projectContext) {
        StringBuilder inputForAI = new StringBuilder();
        for (FileObject file : projectContext) {
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
    
    public static String getTextFilesContext(List<FileObject> scope) {
        StringBuilder inputForAI = new StringBuilder();
        for (FileObject file : getFilesContextList(scope)) {
            if (!file.getMIMEType().startsWith("image")) {
                try {
                    String text = file.asText();
                    if ("java".equals(file.getExt()) && pm.isExcludeJavadocEnabled()) {
                        text = removeJavadoc(text);
                    }
                    inputForAI.append("File: ").append(file.getNameExt()).append("\n");
                    inputForAI.append(text);
                    inputForAI.append("\n\n");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return inputForAI.toString();
    }

    public static List<String> getImageFilesContext(List<FileObject> scope) {
        List<String> base64ImageUrls = new ArrayList<>();
        for (FileObject file : getFilesContextList(scope)) {
            if (file.getMIMEType().startsWith("image")) {
                try (InputStream is = file.getInputStream()) {
                    byte[] imageBytes = is.readAllBytes();
                    String base64 = Base64.getEncoder().encodeToString(imageBytes);
                    String mimeType = file.getMIMEType();
                    String base64Url = "data:" + mimeType + ";base64," + base64;
                    base64ImageUrls.add(base64Url);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return base64ImageUrls;
    }

    public static List<FileObject> getFilesContextList(List<FileObject> scope) {
        List<FileObject> sourceFiles = new ArrayList<>();
        boolean includeNestedFiles = scope.stream()
                .anyMatch(fo -> fo.getPath().contains("src/main/webapp")
                || fo.getPath().endsWith("src/main")
                );
        // Function to collect files recursively
        if (includeNestedFiles) {
            for (FileObject selectedFile : scope) {
                if (selectedFile.isFolder()) {
                    collectNestedFiles(selectedFile, sourceFiles);
                } else if (selectedFile.isData() && pm.getFileExtensionListToInclude().contains(selectedFile.getExt())) {
                    sourceFiles.add(selectedFile);
                }
            }
        } else {
            // Collect only immediate children files
            sourceFiles.addAll(scope.stream()
                    .filter(FileObject::isFolder)
                    .flatMap(packageFolder -> Arrays.stream(packageFolder.getChildren())
                    .filter(FileObject::isData)
                    .filter(file -> pm.getFileExtensionListToInclude().contains(file.getExt())))
                    .collect(Collectors.toSet()));

            sourceFiles.addAll(scope.stream()
                    .filter(FileObject::isData)
                    .filter(file -> pm.getFileExtensionListToInclude().contains(file.getExt()))
                    .collect(Collectors.toSet()));
        }

        return sourceFiles;
    }

    private static void collectNestedFiles(FileObject folder, List<FileObject> sourceFiles) {
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

}
