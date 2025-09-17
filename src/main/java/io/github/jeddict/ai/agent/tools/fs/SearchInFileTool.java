package io.github.jeddict.ai.agent.tools.fs;

import dev.langchain4j.agent.tool.Tool;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.file.PathUtils;

public class SearchInFileTool extends BaseFileSystemTool {

    public SearchInFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "SearchInFileTool_searchInFile",
        value = "Search for a regex pattern in a file by path"
    )
    public String searchInFile(String path, String pattern) throws Exception {
        progress("🔎 Looking for '" + pattern + "' inside '" + path + "'");
        String content = PathUtils.readString(Paths.get(basedir, path), Charset.defaultCharset());
        Matcher m = Pattern.compile(pattern).matcher(content);
        StringBuilder result = new StringBuilder();
        while (m.find()) {
            result.append("Match at ").append(m.start())
                    .append(": ").append(m.group()).append("\n");
        }
        return result.length() > 0 ? result.toString() : "No matches found";
    }
}
