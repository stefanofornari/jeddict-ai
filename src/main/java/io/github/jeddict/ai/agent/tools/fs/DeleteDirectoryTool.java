package io.github.jeddict.ai.agent.tools.fs;

import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeleteDirectoryTool extends BaseFileSystemTool {

    public DeleteDirectoryTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "DeleteDirectoryTool_deleteDirectory",
        value = "Delete a directory at the given path (must be empty)"
    )
    public String deleteDirectory(String path)
    throws Exception {
        progress("🗑️ Attempting to delete directory: " + path);
        try {
            Path dirPath = fullPath(path);
            if (!Files.exists(dirPath)) {
                progress("⚠️ Directory not found: " + path);
                return "Directory not found: " + path;
            }
            if (!Files.isDirectory(dirPath)) {
                progress("⚠️ Not a directory: " + path);
                return "Not a directory: " + path;
            }

            Files.delete(dirPath);
            progress("✅ Directory deleted successfully: " + path);
            return "Directory deleted";
        } catch (IOException e) {
            progress("❌ Directory deletion failed: " + e.getMessage() + " in " + path);
            throw e;
        }
    }
}
