package io.github.jeddict.ai.agent.tools.code;

import dev.langchain4j.agent.tool.Tool;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.JavaSource;

public class ListClassesInFileTool extends BaseCodeTool {

    public ListClassesInFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "ListClassesInFileTool_listClassesInFile",
        value = "List all classes declared in a given Java file by path"
    )
    public String listClassesInFile(String path) throws Exception {
        progress("Listing classes in " + path);
        return withJavaSource(path, javaSource -> {
            final StringBuilder result = new StringBuilder();
            javaSource.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                List<TypeElement> classes = ElementFilter.typesIn(cc.getTopLevelElements());
                for (TypeElement clazz : classes) {
                    result.append("Class: ").append(clazz.getQualifiedName()).append("\n");
                }
            }, true);
            return result.toString();
        }, false).toString();

//.replace("File not found", "No classes found").replace("Not a Java source file", "No classes found");
    }
}
