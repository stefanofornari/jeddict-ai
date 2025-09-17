package io.github.jeddict.ai.agent.tools.fs;

import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.file.PathUtils;

public class ReadFileTool extends BaseFileSystemTool {

    public ReadFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "ReadFileTool_readFile",
        value = "Read the content of a file by path"
    )
    public String readFile(String path) throws Exception {
        progress("📖 Reading file " + path);
        try {
            String content = PathUtils.readString(fullPath(path), Charset.defaultCharset());
            return content;
        } catch (IOException e) {
            progress("❌ Failed to read file: " + e.getMessage());
            throw e;
        }
    }
}
