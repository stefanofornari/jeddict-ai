/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.util;

import io.github.jeddict.ai.settings.PreferencesManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Shiwani Gupta
 */
public class ProjectUtils {

    public static List<FileObject> getSourceFiles(Project project) {
        List<FileObject> sourceFiles = new ArrayList<>();
        collectFiles(project.getProjectDirectory(), project.getProjectDirectory(), sourceFiles,
                new HashSet<>(PreferencesManager.getInstance().getFileExtensionListToInclude()),
                new HashSet<>(PreferencesManager.getInstance().getExcludeDirList()));
        return sourceFiles;
    }

    public static void collectFiles(FileObject baseDir, FileObject folder, List<FileObject> sourceFiles, Set<String> fileExtensionListToInclude, Set<String> excludes) {
        for (FileObject file : folder.getChildren()) {
            String relativePath = getRelativePath(baseDir, file);
            boolean isExcluded = excludes.stream().filter(s -> !s.trim().isEmpty()).anyMatch(relativePath::startsWith);
            if (isExcluded) {
                continue;
            }
            if (file.isFolder()) {
                collectFiles(baseDir, file, sourceFiles, fileExtensionListToInclude, excludes);
            } else if (file.isData()) {
                if (fileExtensionListToInclude.contains(file.getExt())) {
                    sourceFiles.add(file);
                }
            }
        }
    }

    private static String getRelativePath(FileObject baseDir, FileObject file) {
        String basePath = baseDir.getPath();
        String filePath = file.getPath();
        if (filePath.startsWith(basePath)) {
            return filePath.substring(basePath.length() + 1);
        }
        return filePath;
    }
}
