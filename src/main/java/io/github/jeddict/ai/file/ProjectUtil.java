/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.file;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import static org.netbeans.api.java.project.JavaProjectConstants.SOURCES_HINT_TEST;
import static org.netbeans.api.java.project.JavaProjectConstants.SOURCES_TYPE_JAVA;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Gaurav Gupta
 */
public class ProjectUtil {
    
        public static SourceGroup[] getJavaSourceGroups(Project project) {
        Parameters.notNull("project", project);
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(SOURCES_TYPE_JAVA);
        Set<SourceGroup> testGroups = getTestSourceGroups(sourceGroups);
        List<SourceGroup> result = new ArrayList<>();
        for (SourceGroup sourceGroup : sourceGroups) {
            if (!testGroups.contains(sourceGroup)) {
                result.add(sourceGroup);
            }
        }
        return result.toArray(new SourceGroup[result.size()]);
    }
        
           public static Set<SourceGroup> getTestSourceGroups(Project project) {
        return getTestSourceGroups(project, true);
    }

    public static Set<SourceGroup> getTestSourceGroups(Project project, boolean create) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<SourceGroup> testGroups = getTestSourceGroups(sourceGroups);
        if (testGroups.isEmpty() && create) {
            if (SourceGroupModifier.createSourceGroup(project, SOURCES_TYPE_JAVA, SOURCES_HINT_TEST) != null) {
                return getTestSourceGroups(project, false);
            } else {
                throw new IllegalStateException("Test Source group creation failed");
            }
        }
        return testGroups;
    }

    public static SourceGroup getTestSourceGroup(Project project) {
        return getTestSourceGroups(project, true).stream().findAny().get();
    }
    
        private static Map<FileObject, SourceGroup> createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map result;
        if (sourceGroups.length == 0) {
            result = Collections.EMPTY_MAP;
        } else {
            result = new HashMap(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }
        
    private static Set<SourceGroup> getTestSourceGroups(SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<>();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map<FileObject, SourceGroup> foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return Collections.emptyList();
        }
        List<SourceGroup> result = new ArrayList<>();
        List<FileObject> sourceRoots = getFileObjects(rootURLs);
        for (FileObject sourceRoot : sourceRoots) {
            SourceGroup srcGroup = foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }
    
    
    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<>();
        for (URL url : urls) {
            FileObject sourceRoot = URLMapper.findFileObject(url);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                Logger.getLogger(SourceGroup.class.getName()).log(Level.INFO, "No FileObject found for the following URL: " + url);
            }
        }
        return result;
    }
    
        public static SourceGroup getFolderSourceGroup(FileObject folder) {
        Project project = FileOwnerQuery.getOwner(folder);
        return getFolderSourceGroup(
                ProjectUtils.getSources(project).getSourceGroups(SOURCES_TYPE_JAVA),
                folder
        );
    }
        
        

    /**
     * Gets the {@link SourceGroup} of the given <code>folder</code>.
     *
     * @param sourceGroups the source groups to search; must not be null.
     * @param folder the folder whose source group is to be get; must not be
     * null.
     * @return the source group containing the given <code>folder</code> or null
     * if not found.
     */
    public static SourceGroup getFolderSourceGroup(SourceGroup[] sourceGroups, FileObject folder) {
        Parameters.notNull("sourceGroups", sourceGroups); //NOI18N
        Parameters.notNull("folder", folder); //NOI18N
        for (int i = 0; i < sourceGroups.length; i++) {
            if (org.openide.filesystems.FileUtil.isParentOf(sourceGroups[i].getRootFolder(), folder)
                    || sourceGroups[i].getRootFolder().equals(folder)) {
                return sourceGroups[i];
            }
        }
        return null;
    }

    
    /**
     * Converts the path of the given <code>folder</code> to a package name.
     *
     * @param sourceGroup the source group for the folder; must not be null.
     * @param folder the folder to convert; must not be null.
     * @return the package name of the given <code>folder</code>.
     */
    public static String getPackageForFolder(SourceGroup sourceGroup, FileObject folder) {
        Parameters.notNull("sourceGroup", sourceGroup); //NOI18N
        Parameters.notNull("folder", folder); //NOI18N

        String relative = org.openide.filesystems.FileUtil.getRelativePath(sourceGroup.getRootFolder(), folder);
        if (relative != null) {
            return relative.replace('/', '.'); // NOI18N
        } else {
            return ""; // NOI18N
        }
    }

    public static String getPackageForFolder(FileObject folder) {
        Project project = FileOwnerQuery.getOwner(folder);
        SourceGroup[] sources = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup sg = findSourceGroupForFile(sources, folder);
        if (sg != null) {
            return getPackageForFolder(sg, folder);
        } else {
            return "";          //NOI18N
        }
    }
    
    
    public static SourceGroup findSourceGroupForFile(Project project, FileObject folder) {
        return findSourceGroupForFile(getJavaSourceGroups(project), folder);
    }

    /**
     * Gets the {@link SourceGroup} of the given <code>folder</code>.
     *
     * @param sourceGroups the source groups to search; must not be null.
     * @param folder the folder whose source group is to be get; must not be
     * null.
     * @return the source group containing the given <code>folder</code> or null
     * if not found.
     */
    public static SourceGroup findSourceGroupForFile(SourceGroup[] sourceGroups, FileObject folder) {
        for (int i = 0; i < sourceGroups.length; i++) {
            if (org.openide.filesystems.FileUtil.isParentOf(sourceGroups[i].getRootFolder(), folder)
                    || sourceGroups[i].getRootFolder().equals(folder)) {
                return sourceGroups[i];
            }
        }
        return null;
    }

    public static SourceGroup findSourceGroupForFile(FileObject file) {
        Parameters.notNull("file", file); //NOI18N
        Project project = FileOwnerQuery.getOwner(file);
        for (SourceGroup sourceGroup : getJavaSourceGroups(project)) {
            if (org.openide.filesystems.FileUtil.isParentOf(sourceGroup.getRootFolder(), file)) {
                return sourceGroup;
            }
        }
        return null;
    }
}
