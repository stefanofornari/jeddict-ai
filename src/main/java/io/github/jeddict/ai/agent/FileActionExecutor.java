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

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

import java.io.OutputStream;
import java.io.PrintWriter;

public class FileActionExecutor {

    public static void applyFileActionsToProject(Project project, FileAction action) throws Exception {
        FileObject projectDir = project.getProjectDirectory();
        String relativePath = action.getPath().replace("\\", "/");
        FileObject targetFile = projectDir.getFileObject(relativePath);
        String[] pathParts = relativePath.split("/");
        String fileName = pathParts[pathParts.length - 1];

        FileObject parentFolder = projectDir;
        for (int i = 0; i < pathParts.length - 1; i++) {
            FileObject subFolder = parentFolder.getFileObject(pathParts[i]);
            if (subFolder == null) {
                subFolder = parentFolder.createFolder(pathParts[i]);
            }
            parentFolder = subFolder;
        }

        switch (action.getAction()) {
            case "create" -> {
                if (targetFile == null) {
                    targetFile = parentFolder.createData(fileName);
                }
                writeContentToFile(targetFile, action.getContent());
            }
            case "update" -> {
                if (targetFile != null) {
                    writeContentToFile(targetFile, action.getContent());
                } else {
                    // If not found, treat update as create
                    targetFile = parentFolder.createData(fileName);
                    writeContentToFile(targetFile, action.getContent());
                }
            }
            case "delete" -> {
                if (targetFile != null) {
                    targetFile.delete();
                }
            }
            default -> {
                System.err.println("Unknown action: " + action.getAction());
            }
        }
    }

    private static void writeContentToFile(FileObject file, String content) throws Exception {
        try (OutputStream os = file.getOutputStream(); PrintWriter writer = new PrintWriter(os)) {
            writer.write(content);
        }
    }
}
