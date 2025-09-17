package io.github.jeddict.ai.agent.tools.fs;

import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateFileTool extends BaseFileSystemTool {

    public CreateFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "CreateFileTool_createFile",
        value = "Create a new file at the given path with optional content"
    )
    public String createFile(String path, String content) throws Exception {
        progress("📄 Creating new file: " + path);
        try {
            Path filePath = Paths.get(basedir, path);
            if (Files.exists(filePath)) {
                progress("⚠️ File already exists: " + path);
                return "File already exists: " + path;
            }

            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content != null ? content : "");

            progress("✅ File created successfully: " + path);
            return "File created";
        } catch (IOException e) {
            progress("❌ File creation failed: " + e.getMessage() + " in file: " + path);
            throw e;
        }
    }
}
