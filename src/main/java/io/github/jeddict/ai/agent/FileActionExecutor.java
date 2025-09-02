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

import io.github.jeddict.ai.util.DiffUtil;
import java.io.File;
import javax.swing.JEditorPane;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.diff.StreamSource;

public class FileActionExecutor {

    /**
     * Logger for the {@code FileActionExecutor}.
     */
    public static final Logger LOG = Logger.getLogger(FileActionExecutor.class.getCanonicalName());

    public static void applyFileActionsToProject(JEditorPane blockPane, Project project, FileAction action) throws Exception {
        LOG.finest("action " + action.getAction() + " with content >" + action.getContent() + "<");

        final FileObject projectDir = project.getProjectDirectory();
        final String relativePath = action.getPath().replace("\\", "/");
        final FileObject targetFile = projectDir.getFileObject(relativePath);
        final String[] pathParts = relativePath.split("/");
        final String fileName = pathParts[pathParts.length - 1];

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
                FileObject realTarget = (targetFile == null)
                                      ? parentFolder.createData(fileName)
                                      : targetFile;
                //writeContentToFile(realFile, action.getContent());
            }
            case "update" -> {
                if (targetFile != null) {
                    //writeContentToFile(targetFile, action.getContent());
                    final StreamSource left = StreamSource.createSource(
                        "latest", "Assistant Code", targetFile.getMIMEType(), new StringReader(action.getContent())
                    );
                    final StreamSource right = StreamSource.createSource(
                        "original", targetFile.toString(), targetFile.getMIMEType(), new File(targetFile.getPath())
                    );

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            DiffUtil.diffWithOriginal(action.getContent(), targetFile, blockPane);
                        }
                    });
                } else {
                    // If not found, treat update as create
                    // writeContentToFile(parentFolder.createData(fileName), action.getContent());
                }
            }
            case "delete" -> {
                if (targetFile != null) {
                    //targetFile.delete();
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
