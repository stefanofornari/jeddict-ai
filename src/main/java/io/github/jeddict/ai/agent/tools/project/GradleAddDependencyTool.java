package io.github.jeddict.ai.agent.tools.project;

import dev.langchain4j.agent.tool.Tool;
import java.io.OutputStream;
import java.nio.file.Paths;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class GradleAddDependencyTool extends BaseBuildTool {

    public GradleAddDependencyTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "GradleAddDependencyTool_addDependency",
        value = "Add a dependency to the build.gradle file"
    )
    public String addDependency(String configuration, String dependencyNotation)
    throws Exception {
        progress("Adding dependency: " + configuration + ":" + dependencyNotation);
        try {
            FileObject projectDir = FileUtil.toFileObject(Paths.get(basedir));
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                progress("build.gradle not found in project directory");
                return "build.gradle not found in project directory";
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            String dependencyString = configuration + " '" + dependencyNotation + "'";

            if (content.contains(dependencyString)) {
                progress("Dependency already exists: " + dependencyString);
                return "Dependency already exists in build.gradle";
            }

            // Find dependencies block and add the dependency inside it
            String updatedContent;
            int dependenciesIndex = content.indexOf("dependencies {");
            if (dependenciesIndex == -1) {
                // No dependencies block, add one at the end
                updatedContent = content + "\n\ndependencies {\n    " + dependencyString + "\n}\n";
            } else {
                int insertIndex = content.indexOf("}", dependenciesIndex);
                if (insertIndex == -1) {
                    // Malformed file? Just append
                    updatedContent = content + "\n    " + dependencyString + "\n";
                } else {
                    updatedContent = content.substring(0, insertIndex) + "    " + dependencyString + "\n" + content.substring(insertIndex);
                }
            }

            // Write changes back to build.gradle
            try (OutputStream os = gradleFile.getOutputStream()) {
                os.write(updatedContent.getBytes());
            }

            progress("Dependency added successfully: " + dependencyString);
            return "Dependency added successfully to build.gradle";
        } catch (Exception e) {
            progress("Failed to add dependency: " + e.getMessage());

            throw e;
        }
    }
}
