package io.github.jeddict.ai.agent.tools.fs;

import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeleteFileTool extends BaseFileSystemTool {

    public DeleteFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "DeleteFileTool_deleteFile",
        value = "Delete a file at the given path"
    )
    public String deleteFile(String path) throws Exception {
        progress("🗑️ Attempting to delete file: " + path);
        try {
            Path filePath = fullPath(path);
            if (!Files.exists(filePath)) {
                progress("⚠️ File not found: " + path);
                return "File not found: " + path;
            }

            Files.delete(filePath);
            progress("✅ File deleted successfully: " + path);
            return "File deleted";
        } catch (IOException e) {
            progress("❌ File deletion failed: " + e.getMessage() + " in file: " + path);
            throw e;
        }
    }
}
