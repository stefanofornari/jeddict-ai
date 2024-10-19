/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.util;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Shiwani Gupta
 */
public class ProjectUtils {

    public static List<FileObject> getSourceFiles(Project project) {
        List<FileObject> sourceFiles = new ArrayList<>();
        SourceGroup[] sourceGroups = org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups("java");
        for (SourceGroup sourceGroup : sourceGroups) {
            if (sourceGroup.getRootFolder().getParent().getName().equals("test")) {
                continue;
            }
            collectFiles(sourceGroup.getRootFolder(), sourceFiles);
        }
        return sourceFiles;
    }

    public static void collectFiles(FileObject folder, List<FileObject> sourceFiles) {
        for (FileObject file : folder.getChildren()) {
            if (file.isFolder()) {
                collectFiles(file, sourceFiles);
            } else if (file.isData()) {
                sourceFiles.add(file);
            }
        }
    }
}
