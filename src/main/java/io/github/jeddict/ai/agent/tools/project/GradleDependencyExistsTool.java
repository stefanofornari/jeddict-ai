package io.github.jeddict.ai.agent.tools.project;

import dev.langchain4j.agent.tool.Tool;
import org.openide.filesystems.FileObject;

public class GradleDependencyExistsTool extends BaseBuildTool {

    public GradleDependencyExistsTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "GradleDependencyExistsTool_dependencyExists",
        value = "Check if a dependency exists in the build.gradle file"
    )
    public boolean dependencyExists(String configuration, String dependencyNotation)
    throws Exception {
        progress("Checking dependency existence: " + configuration + ":" + dependencyNotation);
        try {
            FileObject projectDir = projectFileObject();
            FileObject gradleFile = projectDir.getFileObject("build.gradle");
            if (gradleFile == null || !gradleFile.isValid()) {
                progress("build.gradle not found in project directory");
                return false;
            }

            // Read the existing content
            String content = new String(gradleFile.asBytes());

            String dependencyString = configuration + " '" + dependencyNotation + "'";

            boolean exists = content.contains(dependencyString);
            progress((exists ? "Dependency exists" : "Dependency does not exist") + ": " + dependencyString);
            return exists;
        } catch (Exception e) {
            progress("Failed to check dependency existence: " + e.getMessage());

            throw e;
        }
    }
}
