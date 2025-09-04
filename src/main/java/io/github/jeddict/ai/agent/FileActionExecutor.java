/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.agent;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

import java.io.OutputStream;
import java.io.PrintWriter;

public class FileActionExecutor {

    public static void applyFileActionsToProject(Project project, FileAction action) throws Exception {
        FileObject projectDir = project.getProjectDirectory();
            String relativePath = action.path().replace("\\", "/");
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

            switch (action.action()) {
                case "create" -> {
                    if (targetFile == null) {
                        targetFile = parentFolder.createData(fileName);
                    }
                    writeContentToFile(targetFile, action.content());
                }
                case "update" -> {
                    if (targetFile != null) {
                        writeContentToFile(targetFile, action.content());
                    } else {
                        // If not found, treat update as create
                        targetFile = parentFolder.createData(fileName);
                        writeContentToFile(targetFile, action.content());
                    }
                }
                case "delete" -> {
                    if (targetFile != null) {
                        targetFile.delete();
                    }
                }
                default -> {
                    System.err.println("Unknown action: " + action.content());
                }
            }
    }

    private static void writeContentToFile(FileObject file, String content) throws Exception {
        try (OutputStream os = file.getOutputStream();
             PrintWriter writer = new PrintWriter(os)) {
            writer.write(content);
        }
    }
}