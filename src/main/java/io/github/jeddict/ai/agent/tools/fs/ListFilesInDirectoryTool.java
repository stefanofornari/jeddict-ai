package io.github.jeddict.ai.agent.tools.fs;

import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ListFilesInDirectoryTool extends BaseFileSystemTool {

    public ListFilesInDirectoryTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "ListFilesInDirectoryTool_listFilesInDirectory",
        value = "List all files and directories inside a given directory path"
    )
    public String listFilesInDirectory(String path) throws Exception {
        progress("📂 Listing contents of directory: " + path);
        try {
            Path dirPath = fullPath(path);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                progress("⚠️ Directory not found: " + path);
                return "Directory not found: " + path;
            }

            StringBuilder result = new StringBuilder(dirPath.getFileName() + ":\n");
            Files.list(dirPath).forEach(p -> {
                result.append(" - ").append(p.getFileName())
                        .append(Files.isDirectory(p) ? "/" : "")
                        .append("\n");
            });

            progress("✅ Directory listed successfully: " + path);
            return result.toString();
        } catch (IOException e) {
            progress("❌ Failed to list directory: " + e.getMessage() + " in " + path);
            throw e;
        }
    }
}
