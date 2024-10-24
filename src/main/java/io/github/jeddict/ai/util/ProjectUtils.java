/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
